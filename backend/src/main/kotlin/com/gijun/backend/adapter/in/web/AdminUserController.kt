package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.user.*
import com.gijun.backend.application.service.AdminUserService
import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.user.entities.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(
    name = "👥 User Management", 
    description = "관리자용 사용자 계정 관리 API"
)
class AdminUserController(
    private val adminUserService: AdminUserService,
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping
    @Operation(
        summary = "📋 사용자 목록 조회",
        description = """
            **시스템에 등록된 사용자 목록을 페이징하여 조회합니다.**
            
            📊 **조회 옵션:**
            - 📄 **페이징**: page, size 파라미터로 페이지네이션
            - 🔍 **필터링**: 상태, 역할, 검색어로 필터링
            - 📈 **정렬**: 생성일자, 마지막 로그인 등으로 정렬
            
            🔐 **권한**: ADMIN_USERS 읽기 권한 필요
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 사용자 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserListResponse::class),
                    examples = [
                        ExampleObject(
                            name = "사용자 목록",
                            value = """{
                                "users": [
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "username": "admin",
                                        "email": "admin@webpos.com",
                                        "roles": ["ADMIN", "USER"],
                                        "userStatus": "ACTIVE",
                                        "isEmailVerified": true,
                                        "lastLoginAt": "2025-06-07T21:00:00",
                                        "failedLoginAttempts": 0,
                                        "isLocked": false
                                    }
                                ],
                                "totalCount": 1,
                                "page": 0,
                                "size": 20
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getUsers(
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "사용자 상태 필터", example = "ACTIVE")
        @RequestParam(required = false) status: String?,
        
        @Parameter(description = "역할 필터", example = "ADMIN")
        @RequestParam(required = false) role: String?,
        
        @Parameter(description = "검색어", example = "admin")
        @RequestParam(required = false) search: String?,
        
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserListResponse> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.READ
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val result = adminUserService.getAllUsers(page, size, status, role, search)
            
            val userDtos = result.users.map { user ->
                AdminUserDto(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    roles = user.roles.map { it.name },
                    userStatus = user.userStatus.name,
                    organizationId = user.organizationId,
                    organizationType = user.organizationType,
                    isEmailVerified = user.isEmailVerified(),
                    lastLoginAt = user.lastLoginAt,
                    failedLoginAttempts = user.failedLoginAttempts,
                    isLocked = user.isLocked(),
                    lockedUntil = user.lockedUntil,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
            
            return ResponseEntity.ok(UserListResponse(
                users = userDtos,
                totalCount = result.totalCount,
                page = result.page,
                size = result.size
            ))
            
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 목록 조회 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @PostMapping
    @Operation(
        summary = "👤 새 사용자 생성",
        description = """
            **시스템에 새로운 사용자를 생성합니다.**
            
            👨‍💼 **생성 절차:**
            1. 사용자 정보 유효성 검증
            2. 중복 사용자명/이메일 확인
            3. 패스워드 암호화 및 저장
            4. 역할 및 권한 할당
            5. 이메일 인증 링크 발송
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "✅ 사용자 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AdminUserDto::class)
                )]
            )
        ]
    )
    suspend fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.WRITE
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val newUser = adminUserService.createUser(
                username = request.username.trim(),
                email = request.email.trim(),
                password = request.password,
                roles = request.roles,
                createdBy = username
            )
            
            val userDto = AdminUserDto(
                id = newUser.id,
                username = newUser.username,
                email = newUser.email,
                roles = newUser.roles.map { it.name },
                userStatus = newUser.userStatus.name,
                organizationId = newUser.organizationId,
                organizationType = newUser.organizationType,
                isEmailVerified = newUser.isEmailVerified(),
                lastLoginAt = newUser.lastLoginAt,
                failedLoginAttempts = newUser.failedLoginAttempts,
                isLocked = newUser.isLocked(),
                lockedUntil = newUser.lockedUntil,
                createdAt = newUser.createdAt,
                updatedAt = newUser.updatedAt
            )
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto)
            
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "✏️ 사용자 정보 수정",
        description = """
            **기존 사용자의 정보를 수정합니다.**
            
            📝 **수정 가능 항목:**
            - 사용자명
            - 이메일 주소
            - 역할 및 권한
            - 계정 상태
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    suspend fun updateUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserRequest,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.WRITE
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val updatedUser = adminUserService.updateUser(
                userId = userId,
                username = request.username.trim(),
                email = request.email.trim(),
                roles = request.roles,
                userStatus = request.userStatus,
                updatedBy = username
            )
            
            val userDto = AdminUserDto(
                id = updatedUser.id,
                username = updatedUser.username,
                email = updatedUser.email,
                roles = updatedUser.roles.map { it.name },
                userStatus = updatedUser.userStatus.name,
                organizationId = updatedUser.organizationId,
                organizationType = updatedUser.organizationType,
                isEmailVerified = updatedUser.isEmailVerified(),
                lastLoginAt = updatedUser.lastLoginAt,
                failedLoginAttempts = updatedUser.failedLoginAttempts,
                isLocked = updatedUser.isLocked(),
                lockedUntil = updatedUser.lockedUntil,
                createdAt = updatedUser.createdAt,
                updatedAt = updatedUser.updatedAt
            )
            
            return ResponseEntity.ok(userDto)
            
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 수정 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "🗑️ 사용자 삭제",
        description = """
            **사용자 계정을 삭제합니다.**
            
            ⚠️ **주의사항:**
            - 삭제된 사용자는 복구할 수 없습니다
            - 관련된 데이터는 익명화 처리됩니다
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    suspend fun deleteUser(
        @Parameter(description = "삭제할 사용자 ID", required = true)
        @PathVariable userId: String,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.DELETE
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            adminUserService.deleteUser(userId, username)
            return ResponseEntity.noContent().build()
            
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 삭제 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @PostMapping("/{userId}/roles")
    @Operation(
        summary = "🔐 역할 할당",
        description = """
            **사용자에게 새로운 역할을 할당합니다.**
            
            🎭 **할당 가능한 역할:**
            - ADMIN: 시스템 관리자
            - HEADQUARTERS_ADMIN: 본사 관리자
            - STORE_ADMIN: 매장 관리자
            - USER: 일반 사용자
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    suspend fun assignRoles(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: String,
        @Valid @RequestBody request: AssignRoleRequest,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.WRITE
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val updatedUser = adminUserService.assignRoles(
                userId = userId,
                roles = request.roles,
                updatedBy = username
            )
            
            val userDto = AdminUserDto(
                id = updatedUser.id,
                username = updatedUser.username,
                email = updatedUser.email,
                roles = updatedUser.roles.map { it.name },
                userStatus = updatedUser.userStatus.name,
                organizationId = updatedUser.organizationId,
                organizationType = updatedUser.organizationType,
                isEmailVerified = updatedUser.isEmailVerified(),
                lastLoginAt = updatedUser.lastLoginAt,
                failedLoginAttempts = updatedUser.failedLoginAttempts,
                isLocked = updatedUser.isLocked(),
                lockedUntil = updatedUser.lockedUntil,
                createdAt = updatedUser.createdAt,
                updatedAt = updatedUser.updatedAt
            )
            
            return ResponseEntity.ok(userDto)
            
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "역할 할당 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @PostMapping("/{userId}/unlock")
    @Operation(
        summary = "🔓 사용자 잠금 해제",
        description = """
            **잠긴 사용자 계정을 해제합니다.**
            
            🔓 **해제 효과:**
            - 로그인 실패 카운터 초기화
            - 계정 잠금 상태 해제
            - 즉시 로그인 가능
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    suspend fun unlockUser(
        @Parameter(description = "잠금 해제할 사용자 ID", required = true)
        @PathVariable userId: String,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.WRITE
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val unlockedUser = adminUserService.unlockUser(userId, username)
            
            val userDto = AdminUserDto(
                id = unlockedUser.id,
                username = unlockedUser.username,
                email = unlockedUser.email,
                roles = unlockedUser.roles.map { it.name },
                userStatus = unlockedUser.userStatus.name,
                organizationId = unlockedUser.organizationId,
                organizationType = unlockedUser.organizationType,
                isEmailVerified = unlockedUser.isEmailVerified(),
                lastLoginAt = unlockedUser.lastLoginAt,
                failedLoginAttempts = unlockedUser.failedLoginAttempts,
                isLocked = unlockedUser.isLocked(),
                lockedUntil = unlockedUser.lockedUntil,
                createdAt = unlockedUser.createdAt,
                updatedAt = unlockedUser.updatedAt
            )
            
            return ResponseEntity.ok(userDto)
            
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 잠금 해제 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "👤 사용자 상세 조회",
        description = """
            **특정 사용자의 상세 정보를 조회합니다.**
            
            📋 **조회 정보:**
            - 기본 프로필 정보
            - 권한 및 역할
            - 계정 상태 및 보안 정보
            - 조직 소속 정보
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["👥 User Management"]
    )
    suspend fun getUser(
        @Parameter(description = "조회할 사용자 ID", required = true)
        @PathVariable userId: String,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, "ADMIN_USERS", PermissionType.READ
        )
        
        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }
        
        try {
            val user = adminUserService.getUserById(userId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
            
            val userDto = AdminUserDto(
                id = user.id,
                username = user.username,
                email = user.email,
                roles = user.roles.map { it.name },
                userStatus = user.userStatus.name,
                organizationId = user.organizationId,
                organizationType = user.organizationType,
                isEmailVerified = user.isEmailVerified(),
                lastLoginAt = user.lastLoginAt,
                failedLoginAttempts = user.failedLoginAttempts,
                isLocked = user.isLocked(),
                lockedUntil = user.lockedUntil,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
            
            return ResponseEntity.ok(userDto)
            
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 조회 중 오류가 발생했습니다: ${e.message}")
        }
    }
}

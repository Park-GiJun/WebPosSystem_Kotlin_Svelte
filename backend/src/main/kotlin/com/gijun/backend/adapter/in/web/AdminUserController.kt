package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.*
import com.gijun.backend.application.service.AdminUserService
import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.user.entities.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val adminUserService: AdminUserService,
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping
    suspend fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) search: String?,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserListResponse> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.READ
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
    suspend fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.WRITE
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
    suspend fun updateUser(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.WRITE
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
    suspend fun deleteUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.DELETE
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
    suspend fun assignRoles(
        @PathVariable userId: String,
        @Valid @RequestBody request: AssignRoleRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.WRITE
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
    suspend fun unlockUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.WRITE
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
    suspend fun getUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // 권한 체크
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            "ADMIN_USERS", 
            PermissionType.READ
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

data class UserListResponse(
    val users: List<AdminUserDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class AdminUserDto(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String,
    val organizationId: String?,
    val organizationType: String?,
    val isEmailVerified: Boolean,
    val lastLoginAt: java.time.LocalDateTime?,
    val failedLoginAttempts: Int,
    val isLocked: Boolean,
    val lockedUntil: java.time.LocalDateTime?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class CreateUserRequest(
    @field:jakarta.validation.constraints.NotBlank
    val username: String,
    @field:jakarta.validation.constraints.Email
    val email: String,
    @field:jakarta.validation.constraints.NotBlank
    val password: String,
    val roles: List<String> = listOf("USER")
)

data class UpdateUserRequest(
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String?
)

data class AssignRoleRequest(
    val roles: List<String>
)

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.auth.*
import com.gijun.backend.application.port.`in`.AuthUseCase
import com.gijun.backend.application.port.`in`.LoginCommand
import com.gijun.backend.application.port.`in`.RegisterCommand
import com.gijun.backend.application.service.AuthService
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.common.util.ApiResponse
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

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "🔐 Authentication", 
    description = "사용자 인증 및 계정 관리 API"
)
class AuthController(
    private val authUseCase: AuthUseCase,
    private val authService: AuthService,
    private val jwtUtil: JwtUtil
) {



    @PostMapping("/login")
    @Operation(
        summary = "🔑 로그인",
        description = """
            **사용자 인증을 수행하고 JWT 토큰을 발급합니다.**
            
            🔐 **로그인 절차:**
            1. 사용자명/패스워드 검증
            2. 계정 상태 확인 (잠금, 비활성화 등)
            3. JWT 토큰 생성 및 발급
            4. 로그인 기록 업데이트
            
            🎫 **토큰 정보:**
            - **만료시간**: 24시간
            - **타입**: Bearer Token
            - **사용법**: Authorization 헤더에 "Bearer {token}" 형식으로 전달
            
            🔒 **보안 기능:**
            - 5회 실패 시 계정 잠금 (30분)
            - 로그인 시간 기록
            - 세션 관리
        """,
        tags = ["🔐 Authentication"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 로그인 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AuthResponse::class),
                    examples = [
                        ExampleObject(
                            name = "로그인 성공",
                            summary = "정상적인 로그인이 완료된 경우",
                            value = """{
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "username": "admin",
                                "email": "admin@webpos.com",
                                "roles": ["ADMIN", "USER"],
                                "userStatus": "ACTIVE",
                                "isEmailVerified": true,
                                "lastLoginAt": "2025-06-07T21:00:00",
                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                "expiresIn": 86400
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 실패",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "잘못된 인증정보",
                            value = """{
                                "success": false,
                                "message": "사용자명 또는 패스워드가 올바르지 않습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        ),
                        ExampleObject(
                            name = "계정 잠김",
                            value = """{
                                "success": false,
                                "message": "계정이 잠겨있습니다. 30분 후 다시 시도해주세요",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val command = LoginCommand(
            username = request.username,
            password = request.password
        )

        val authResult = authUseCase.login(command)

        return ResponseEntity.ok(AuthResponse(
            id = authResult.user.id,
            username = authResult.user.username,
            email = authResult.user.email,
            roles = authResult.user.roles.map { it.name },
            userStatus = authResult.user.userStatus.name,
            isEmailVerified = authResult.user.isEmailVerified(),
            lastLoginAt = authResult.user.lastLoginAt,
            token = authResult.token,
            expiresIn = authResult.expiresIn
        ))
    }

    @GetMapping("/me")
    @Operation(
        summary = "👤 내 정보 조회",
        description = """
            **현재 로그인한 사용자의 상세 정보를 조회합니다.**
            
            📋 **조회 정보:**
            - 기본 프로필 (이름, 이메일 등)
            - 권한 및 역할 정보
            - 계정 상태 및 보안 정보
            - 로그인 기록
            
            🔐 **인증 필요:** Bearer Token 필수
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🔐 Authentication"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 사용자 정보 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserProfileResponse::class),
                    examples = [
                        ExampleObject(
                            name = "사용자 정보",
                            value = """{
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "username": "admin",
                                "email": "admin@webpos.com",
                                "roles": ["ADMIN", "USER"],
                                "userStatus": "ACTIVE",
                                "isEmailVerified": true,
                                "lastLoginAt": "2025-06-07T21:00:00",
                                "failedLoginAttempts": 0,
                                "isLocked": false,
                                "lockedUntil": null,
                                "createdAt": "2025-01-01T00:00:00",
                                "updatedAt": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 토큰 없음 또는 만료",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "유효하지 않은 토큰입니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getCurrentUser(
        @Parameter(
            description = "JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserProfileResponse> {
        val token = authorization.removePrefix("Bearer ")
        val user = authUseCase.validateToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(UserProfileResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            roles = user.roles.map { it.name },
            userStatus = user.userStatus.name,
            isEmailVerified = user.isEmailVerified(),
            lastLoginAt = user.lastLoginAt,
            failedLoginAttempts = user.failedLoginAttempts,
            isLocked = user.isLocked(),
            lockedUntil = user.lockedUntil,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        ))
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "🔒 비밀번호 변경",
        description = """
            **현재 사용자의 비밀번호를 변경합니다.**
            
            🔐 **변경 절차:**
            1. 현재 비밀번호 확인
            2. 새 비밀번호 유효성 검증
            3. 암호화 후 저장
            4. 세션 갱신
            
            ⚠️ **보안 요구사항:**
            - 현재 비밀번호 필수 입력
            - 새 비밀번호: 8자 이상, 대소문자/숫자/특수문자 포함
            - 최근 3개 비밀번호와 달라야 함
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🔐 Authentication"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 비밀번호 변경 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": true,
                                "message": "비밀번호가 성공적으로 변경되었습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "❌ 잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "현재 비밀번호 불일치",
                            value = """{
                                "success": false,
                                "message": "현재 비밀번호가 올바르지 않습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 필요",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    suspend fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        @Parameter(
            description = "JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<String> {
        val token = authorization.removePrefix("Bearer ")
        val user = authUseCase.validateToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        authService.changePassword(
            userId = user.id,
            currentPassword = request.currentPassword,
            newPassword = request.newPassword,
            updatedBy = user.username
        )

        return ResponseEntity.ok("Password changed successfully")
    }



    @PostMapping("/unlock/{userId}")
    @Operation(
        summary = "🔓 계정 잠금 해제",
        description = """
            **잠긴 사용자 계정을 해제합니다.**
            
            🔐 **관리자 권한 필요**
            
            🔓 **해제 절차:**
            1. 관리자 권한 확인
            2. 대상 계정 상태 검증
            3. 잠금 해제 처리
            4. 로그 기록
            
            📝 **로그인 실패 카운터도 함께 초기화됩니다.**
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🔐 Authentication"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 계정 잠금 해제 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": true,
                                "message": "계정 잠금이 해제되었습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "🚫 권한 없음 (관리자 권한 필요)",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "관리자 권한이 필요합니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "❌ 사용자를 찾을 수 없음",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    suspend fun unlockUser(
        @Parameter(
            description = "잠금 해제할 사용자 ID", 
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
        )
        @PathVariable userId: String,
        @Parameter(
            description = "관리자 JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<String> {
        val token = authorization.removePrefix("Bearer ")
        val currentUser = authUseCase.validateToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!currentUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        authService.unlockUser(userId, currentUser.username)
        return ResponseEntity.ok("User unlocked successfully")
    }
}

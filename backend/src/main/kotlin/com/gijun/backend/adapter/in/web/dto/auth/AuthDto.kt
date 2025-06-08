package com.gijun.backend.adapter.`in`.web.dto.auth

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "로그인 요청")
data class LoginRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "사용자명", example = "admin", required = true)
    val username: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "패스워드는 필수입니다")
    @Schema(description = "패스워드", example = "password123!", required = true)
    val password: String
)

@Schema(description = "회원가입 요청")
data class RegisterRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "사용자명은 필수입니다")
    @field:jakarta.validation.constraints.Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @Schema(description = "사용자명", example = "newuser", required = true)
    val username: String,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @field:jakarta.validation.constraints.NotBlank(message = "이메일은 필수입니다")
    @Schema(description = "이메일", example = "newuser@example.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "패스워드는 필수입니다")
    @field:jakarta.validation.constraints.Size(min = 8, message = "패스워드는 최소 8자 이상이어야 합니다")
    @Schema(description = "패스워드", example = "password123!", required = true)
    val password: String
)

@Schema(description = "패스워드 변경 요청")
data class ChangePasswordRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "현재 패스워드는 필수입니다")
    @Schema(description = "현재 패스워드", example = "oldpassword123!", required = true)
    val currentPassword: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "새 패스워드는 필수입니다")
    @field:jakarta.validation.constraints.Size(min = 8, message = "패스워드는 최소 8자 이상이어야 합니다")
    @Schema(description = "새 패스워드", example = "newpassword123!", required = true)
    val newPassword: String
)

@Schema(description = "인증 응답")
data class AuthResponse(
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,
    
    @Schema(description = "사용자명", example = "admin")
    val username: String,
    
    @Schema(description = "이메일", example = "admin@webpos.com")
    val email: String,
    
    @Schema(description = "역할 목록", example = "[\"ADMIN\", \"USER\"]")
    val roles: List<String>,
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val userStatus: String,
    
    @Schema(description = "이메일 인증 여부", example = "true")
    val isEmailVerified: Boolean,
    
    @Schema(description = "마지막 로그인 시간")
    val lastLoginAt: LocalDateTime?,
    
    @Schema(description = "JWT 토큰")
    val token: String? = null,
    
    @Schema(description = "토큰 만료 시간(초)", example = "86400")
    val expiresIn: Long? = null
)

@Schema(description = "사용자 프로필 응답")
data class UserProfileResponse(
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,
    
    @Schema(description = "사용자명", example = "admin")
    val username: String,
    
    @Schema(description = "이메일", example = "admin@webpos.com")
    val email: String,
    
    @Schema(description = "역할 목록", example = "[\"ADMIN\", \"USER\"]")
    val roles: List<String>,
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val userStatus: String,
    
    @Schema(description = "이메일 인증 여부", example = "true")
    val isEmailVerified: Boolean,
    
    @Schema(description = "마지막 로그인 시간")
    val lastLoginAt: LocalDateTime?,
    
    @Schema(description = "로그인 실패 횟수", example = "0")
    val failedLoginAttempts: Int,
    
    @Schema(description = "계정 잠금 여부", example = "false")
    val isLocked: Boolean,
    
    @Schema(description = "잠금 해제 시간")
    val lockedUntil: LocalDateTime?,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,
    
    @Schema(description = "수정 시간")
    val updatedAt: LocalDateTime
)

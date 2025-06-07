package com.gijun.backend.adapter.`in`.web.dto.auth

import java.time.LocalDateTime

data class LoginRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Username is required")
    val username: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "Password is required")
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String?,
    val user: AuthUserResponse,
    val expiresAt: LocalDateTime
)

data class RefreshTokenRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

data class RegisterRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Username is required")
    @field:jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,
    
    @field:jakarta.validation.constraints.Email(message = "Invalid email format")
    @field:jakarta.validation.constraints.NotBlank(message = "Email is required")
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "Password is required")
    @field:jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "Confirm password is required")
    val confirmPassword: String
)

data class ChangePasswordRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "New password is required")
    @field:jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "Confirm password is required")
    val confirmPassword: String
)

data class AuthUserResponse(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String,
    val organizationId: String?,
    val organizationType: String?,
    val isEmailVerified: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class LogoutResponse(
    val message: String = "Successfully logged out"
)

data class TokenValidationResponse(
    val valid: Boolean,
    val user: AuthUserResponse?,
    val expiresAt: LocalDateTime?
)

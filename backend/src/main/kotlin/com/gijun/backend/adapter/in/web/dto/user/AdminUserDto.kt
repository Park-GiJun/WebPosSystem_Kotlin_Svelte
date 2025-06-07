package com.gijun.backend.adapter.`in`.web.dto.user

import java.time.LocalDateTime

data class AdminUserDto(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String,
    val organizationId: String?,
    val organizationType: String?,
    val isEmailVerified: Boolean,
    val lastLoginAt: LocalDateTime?,
    val failedLoginAttempts: Int,
    val isLocked: Boolean,
    val lockedUntil: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserListResponse(
    val users: List<AdminUserDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
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

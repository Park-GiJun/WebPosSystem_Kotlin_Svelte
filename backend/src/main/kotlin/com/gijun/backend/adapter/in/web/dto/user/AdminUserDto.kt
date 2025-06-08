package com.gijun.backend.adapter.`in`.web.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "사용자 목록 응답")
data class UserListResponse(
    @Schema(description = "사용자 목록")
    val users: List<AdminUserDto>,
    
    @Schema(description = "전체 사용자 수", example = "50")
    val totalCount: Long,
    
    @Schema(description = "현재 페이지", example = "0")
    val page: Int,
    
    @Schema(description = "페이지 크기", example = "20")
    val size: Int
)

@Schema(description = "관리자용 사용자 정보")
data class AdminUserDto(
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
    
    @Schema(description = "조직 ID")
    val organizationId: String?,
    
    @Schema(description = "조직 타입")
    val organizationType: String?,
    
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

@Schema(description = "사용자 생성 요청")
data class CreateUserRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "사용자명", example = "newuser", required = true)
    val username: String,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일", example = "newuser@example.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "패스워드는 필수입니다")
    @Schema(description = "패스워드", example = "password123!", required = true)
    val password: String,
    
    @Schema(description = "역할 목록", example = "[\"USER\"]")
    val roles: List<String> = listOf("USER")
)

@Schema(description = "사용자 수정 요청")
data class UpdateUserRequest(
    @Schema(description = "사용자명", example = "updateduser")
    val username: String,
    
    @Schema(description = "이메일", example = "updated@example.com")
    val email: String,
    
    @Schema(description = "역할 목록", example = "[\"USER\", \"ADMIN\"]")
    val roles: List<String>,
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val userStatus: String?
)

@Schema(description = "역할 할당 요청")
data class AssignRoleRequest(
    @Schema(description = "할당할 역할 목록", example = "[\"ADMIN\", \"USER\"]")
    val roles: List<String>
)

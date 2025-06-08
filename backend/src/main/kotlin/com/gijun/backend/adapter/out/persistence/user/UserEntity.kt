package com.gijun.backend.adapter.out.persistence.user

import com.gijun.backend.domain.user.vo.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class UserEntity(
    @Id
    val id: String,

    @Column("username")
    val username: String,

    @Column("email")
    val email: String,

    @Column("password_hash")
    val passwordHash: String,

    @Column("roles")
    val roles: String, // JSON 형태로 저장

    @Column("user_status")
    val userStatus: String,

    // 조직 소속 정보 (조직 ID와 타입 추가)
    @Column("organization_id")
    val organizationId: String? = null,

    @Column("organization_type")
    val organizationType: String? = null,

    // Security & Login tracking
    @Column("last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column("failed_login_attempts")
    val failedLoginAttempts: Int = 0,

    @Column("locked_until")
    val lockedUntil: LocalDateTime? = null,

    @Column("email_verified_at")
    val emailVerifiedAt: LocalDateTime? = null,

    // Audit fields
    @Column("is_active")
    val isActive: Boolean = true,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("created_by")
    val createdBy: String? = null,

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_by")
    val updatedBy: String? = null,

    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,

    @Column("deleted_by")
    val deletedBy: String? = null,

    @Version
    val version: Long = 0
)

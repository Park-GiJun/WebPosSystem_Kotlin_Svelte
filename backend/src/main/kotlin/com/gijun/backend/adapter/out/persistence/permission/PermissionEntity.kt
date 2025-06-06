package com.gijun.backend.adapter.out.persistence.permission

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("permissions")
data class PermissionEntity(
    @Id
    val permissionId: String,

    @Column("menu_id")
    val menuId: String,

    @Column("target_type")
    val targetType: String,

    @Column("target_id")
    val targetId: String,

    @Column("permission_type")
    val permissionType: String,

    @Column("granted_at")
    val grantedAt: LocalDateTime = LocalDateTime.now(),

    @Column("granted_by")
    val grantedBy: String,

    @Column("expires_at")
    val expiresAt: LocalDateTime? = null,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
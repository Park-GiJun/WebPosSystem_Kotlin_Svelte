package com.gijun.backend.adapter.out.persistence.permission

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("permissions")
data class PermissionEntity(
    @Id
    val id: String,

    @Column("menu_code")
    val menuCode: String,

    @Column("permission_target_type")
    val permissionTargetType: String,

    @Column("target_id")
    val targetId: String,

    @Column("permission_type")
    val permissionType: String,

    @Column("granted_by")
    val grantedBy: String?,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
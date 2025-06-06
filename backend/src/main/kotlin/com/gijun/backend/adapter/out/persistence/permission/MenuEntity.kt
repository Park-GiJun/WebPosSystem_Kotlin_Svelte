package com.gijun.backend.adapter.out.persistence.permission

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("menus")
data class MenuEntity(
    @Id
    val menuId: String,

    @Column("menu_code")
    val menuCode: String,

    @Column("menu_name")
    val menuName: String,

    @Column("menu_path")
    val menuPath: String,

    @Column("parent_menu_id")
    val parentMenuId: String? = null,

    @Column("menu_level")
    val menuLevel: Int = 1,

    @Column("display_order")
    val displayOrder: Int = 0,

    @Column("icon_name")
    val iconName: String? = null,

    @Column("description")
    val description: String? = null,

    @Column("menu_type")
    val menuType: String = "MENU",

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
    val deletedBy: String? = null
)
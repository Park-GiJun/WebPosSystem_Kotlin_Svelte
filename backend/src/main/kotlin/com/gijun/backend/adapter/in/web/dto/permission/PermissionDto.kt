package com.gijun.backend.adapter.`in`.web.dto.permission

import java.time.LocalDateTime

// Menu DTOs
data class MenuDto(
    val menuId: String,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val parentMenuId: String?,
    val menuLevel: Int,
    val displayOrder: Int,
    val iconName: String?,
    val description: String?,
    val menuType: String,
    val isActive: Boolean,
    val children: List<MenuDto> = emptyList()
)

data class CreateMenuRequest(
    @field:jakarta.validation.constraints.NotBlank
    val menuCode: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val menuName: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val menuPath: String,
    
    val parentMenuId: String?,
    val menuLevel: Int = 1,
    val displayOrder: Int = 0,
    val iconName: String?,
    val description: String?,
    val menuType: String = "MENU"
)

data class UpdateMenuRequest(
    val menuName: String?,
    val menuPath: String?,
    val iconName: String?,
    val description: String?,
    val displayOrder: Int?
)

// Permission DTOs
data class PermissionDto(
    val permissionId: String,
    val menuId: String,
    val targetType: String,
    val targetId: String,
    val permissionType: String,
    val grantedAt: LocalDateTime,
    val grantedBy: String,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean
)

data class GrantPermissionRequest(
    @field:jakarta.validation.constraints.NotBlank
    val menuId: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val targetType: String, // USER, ROLE, STORE, HEADQUARTERS
    
    @field:jakarta.validation.constraints.NotBlank
    val targetId: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val permissionType: String, // READ, WRITE, DELETE, ADMIN
    
    val expiresAt: LocalDateTime?
)

data class RevokePermissionRequest(
    @field:jakarta.validation.constraints.NotBlank
    val permissionId: String,
    
    val reason: String?
)

data class BulkPermissionRequest(
    val permissions: List<GrantPermissionRequest>
)

data class UserMenuPermissionsResponse(
    val userId: String,
    val username: String,
    val menus: List<MenuWithPermissionDto>
)

data class MenuWithPermissionDto(
    val menuId: String,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val menuType: String,
    val permissionType: String?,
    val hasAccess: Boolean,
    val children: List<MenuWithPermissionDto> = emptyList()
)

data class PermissionListResponse(
    val permissions: List<PermissionDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class MenuListResponse(
    val menus: List<MenuDto>,
    val totalCount: Long
)

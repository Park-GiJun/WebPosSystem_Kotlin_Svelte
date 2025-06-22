package com.gijun.backend.domain.permission.entities

import com.gijun.backend.domain.permission.vo.MenuId
import com.gijun.backend.domain.permission.vo.PermissionId
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.user.vo.UserId
import com.gijun.backend.domain.user.enums.UserRole
import java.time.LocalDateTime

data class Permission(
    val permissionId: PermissionId,
    val menuId: MenuId,
    val targetType: PermissionTargetType,
    val targetId: String, // UserId, OrganizationId, or Role name
    val permissionType: PermissionType,
    val grantedBy: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun isValid(): Boolean = isActive

    fun revoke(): Permission =
        this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )

    companion object {
        fun grantToUser(
            menuId: MenuId,
            userId: UserId,
            permissionType: PermissionType,
            grantedBy: String
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.USER,
            targetId = userId.value,
            permissionType = permissionType,
            grantedBy = grantedBy
        )

        fun grantToOrganization(
            menuId: MenuId,
            organizationId: String,
            permissionType: PermissionType,
            grantedBy: String
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.ORGANIZATION,
            targetId = organizationId,
            permissionType = permissionType,
            grantedBy = grantedBy
        )

        fun grantToRole(
            menuId: MenuId,
            role: UserRole,
            permissionType: PermissionType,
            grantedBy: String
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.ROLE,
            targetId = role.name,
            permissionType = permissionType,
            grantedBy = grantedBy
        )
    }
}

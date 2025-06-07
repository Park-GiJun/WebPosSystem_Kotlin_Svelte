package com.gijun.backend.domain.permission.entities

import com.gijun.backend.domain.permission.vo.MenuId
import com.gijun.backend.domain.permission.vo.PermissionId
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.user.vo.UserId
import com.gijun.backend.domain.user.enums.UserRole
import java.time.LocalDateTime

data class Permission(
    val permissionId: PermissionId,
    val menuId: MenuId,
    val targetType: PermissionTargetType,
    val targetId: String, // UserId, StoreId, or HeadquartersId
    val permissionType: PermissionType,
    val grantedAt: LocalDateTime = LocalDateTime.now(),
    val grantedBy: String,
    val expiresAt: LocalDateTime? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun isExpired(): Boolean = expiresAt?.isBefore(LocalDateTime.now()) == true
    fun isValid(): Boolean = isActive && !isExpired()

    fun revoke(revokedBy: String): Permission =
        this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )

    fun extend(newExpiresAt: LocalDateTime?, updatedBy: String): Permission =
        this.copy(
            expiresAt = newExpiresAt,
            updatedAt = LocalDateTime.now()
        )

    companion object {
        fun grantToUser(
            menuId: MenuId,
            userId: UserId,
            permissionType: PermissionType,
            grantedBy: String,
            expiresAt: LocalDateTime? = null
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.USER,
            targetId = userId.value,
            permissionType = permissionType,
            grantedBy = grantedBy,
            expiresAt = expiresAt
        )

        fun grantToStore(
            menuId: MenuId,
            storeId: StoreId,
            permissionType: PermissionType,
            grantedBy: String,
            expiresAt: LocalDateTime? = null
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.STORE,
            targetId = storeId.value,
            permissionType = permissionType,
            grantedBy = grantedBy,
            expiresAt = expiresAt
        )

        fun grantToHeadquarters(
            menuId: MenuId,
            headquartersId: HeadquartersId,
            permissionType: PermissionType,
            grantedBy: String,
            expiresAt: LocalDateTime? = null
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.HEADQUARTERS,
            targetId = headquartersId.value,
            permissionType = permissionType,
            grantedBy = grantedBy,
            expiresAt = expiresAt
        )

        fun grantToRole(
            menuId: MenuId,
            role: UserRole,
            permissionType: PermissionType,
            grantedBy: String,
            expiresAt: LocalDateTime? = null
        ): Permission = Permission(
            permissionId = PermissionId.generate(),
            menuId = menuId,
            targetType = PermissionTargetType.ROLE,
            targetId = role.name,
            permissionType = permissionType,
            grantedBy = grantedBy,
            expiresAt = expiresAt
        )
    }
}
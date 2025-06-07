package com.gijun.backend.application.port.out

import com.gijun.backend.domain.permission.entities.Permission
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType

interface PermissionRepository {
    suspend fun save(permission: Permission): Permission
    suspend fun findById(permissionId: String): Permission?
    suspend fun findByTargetId(targetType: PermissionTargetType, targetId: String): List<Permission>
    suspend fun findByMenuId(menuId: String): List<Permission>
    suspend fun findUserPermissions(userId: String): List<Permission>
    suspend fun findActivePermissions(): List<Permission>
    suspend fun deleteById(permissionId: String)
    suspend fun revokePermission(menuId: String, targetType: PermissionTargetType, targetId: String)
}
package com.gijun.backend.adapter.out.persistence.permission

import com.gijun.backend.domain.permission.entities.Permission
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.permission.vo.MenuId
import com.gijun.backend.domain.permission.vo.PermissionId
import org.springframework.stereotype.Component

@Component
class PermissionMapper {

    fun toDomain(entity: PermissionEntity): Permission {
        return Permission(
            permissionId = PermissionId.fromString(entity.id),
            menuId = MenuId.fromString(entity.menuCode),
            targetType = PermissionTargetType.valueOf(entity.permissionTargetType),
            targetId = entity.targetId,
            permissionType = PermissionType.valueOf(entity.permissionType.uppercase()),
            grantedBy = entity.grantedBy ?: "system",
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Permission): PermissionEntity {
        return PermissionEntity(
            id = domain.permissionId.value,
            menuCode = domain.menuId.value,
            permissionTargetType = domain.targetType.name,
            targetId = domain.targetId,
            permissionType = domain.permissionType.name.lowercase(),
            grantedBy = domain.grantedBy,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
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
            permissionId = PermissionId.fromString(entity.permissionId),
            menuId = MenuId.fromString(entity.menuId),
            targetType = PermissionTargetType.valueOf(entity.targetType),
            targetId = entity.targetId,
            permissionType = PermissionType.valueOf(entity.permissionType),
            grantedAt = entity.grantedAt,
            grantedBy = entity.grantedBy,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Permission): PermissionEntity {
        return PermissionEntity(
            permissionId = domain.permissionId.value,
            menuId = domain.menuId.value,
            targetType = domain.targetType.name,
            targetId = domain.targetId,
            permissionType = domain.permissionType.name,
            grantedAt = domain.grantedAt,
            grantedBy = domain.grantedBy,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
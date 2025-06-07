package com.gijun.backend.adapter.out.persistence.permission

import com.gijun.backend.domain.permission.entities.Menu
import com.gijun.backend.domain.permission.enums.MenuType
import com.gijun.backend.domain.permission.vo.MenuId
import org.springframework.stereotype.Component

@Component
class MenuMapper {

    fun toDomain(entity: MenuEntity): Menu {
        return Menu(
            menuId = MenuId.fromString(entity.menuId),
            menuCode = entity.menuCode,
            menuName = entity.menuName,
            menuPath = entity.menuPath,
            parentMenuId = entity.parentMenuId?.let { MenuId.fromString(it) },
            menuLevel = entity.menuLevel,
            displayOrder = entity.displayOrder,
            iconName = entity.iconName,
            description = entity.description,
            menuType = MenuType.valueOf(entity.menuType),
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            deletedAt = entity.deletedAt,
            deletedBy = entity.deletedBy
        )
    }

    fun toEntity(domain: Menu): MenuEntity {
        return MenuEntity(
            menuId = domain.menuId.value,
            menuCode = domain.menuCode,
            menuName = domain.menuName,
            menuPath = domain.menuPath,
            parentMenuId = domain.parentMenuId?.value,
            menuLevel = domain.menuLevel,
            displayOrder = domain.displayOrder,
            iconName = domain.iconName,
            description = domain.description,
            menuType = domain.menuType.name,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = domain.deletedAt,
            deletedBy = domain.deletedBy
        )
    }
}
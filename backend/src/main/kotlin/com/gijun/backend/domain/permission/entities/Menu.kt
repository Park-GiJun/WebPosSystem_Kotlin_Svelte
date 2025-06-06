package com.gijun.backend.domain.permission.entities

import com.gijun.backend.domain.permission.vo.MenuId
import com.gijun.backend.domain.common.AuditableEntity
import java.time.LocalDateTime

data class Menu(
    val menuId: MenuId,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val parentMenuId: MenuId? = null,
    val menuLevel: Int = 1,
    val displayOrder: Int = 0,
    val iconName: String? = null,
    val description: String? = null,
    val menuType: MenuType = MenuType.MENU,
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    init {
        require(menuCode.isNotBlank()) { "메뉴 코드는 필수입니다." }
        require(menuName.isNotBlank()) { "메뉴명은 필수입니다." }
        require(menuLevel > 0) { "메뉴 레벨은 1 이상이어야 합니다." }
    }

    fun isParentMenu(): Boolean = menuType == MenuType.CATEGORY
    fun hasParent(): Boolean = parentMenuId != null

    fun updateInfo(
        menuName: String? = null,
        menuPath: String? = null,
        iconName: String? = null,
        description: String? = null,
        displayOrder: Int? = null,
        updatedBy: String
    ): Menu = this.copy(
        menuName = menuName ?: this.menuName,
        menuPath = menuPath ?: this.menuPath,
        iconName = iconName ?: this.iconName,
        description = description ?: this.description,
        displayOrder = displayOrder ?: this.displayOrder,
        updatedAt = LocalDateTime.now(),
        updatedBy = updatedBy
    )

    fun activate(updatedBy: String): Menu =
        this.copy(
            isActive = true,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun deactivate(updatedBy: String): Menu =
        this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    companion object {
        fun create(
            menuCode: String,
            menuName: String,
            menuPath: String,
            parentMenuId: MenuId? = null,
            menuLevel: Int = 1,
            displayOrder: Int = 0,
            iconName: String? = null,
            description: String? = null,
            menuType: MenuType = MenuType.MENU,
            createdBy: String
        ): Menu {
            return Menu(
                menuId = MenuId.generate(),
                menuCode = menuCode,
                menuName = menuName,
                menuPath = menuPath,
                parentMenuId = parentMenuId,
                menuLevel = menuLevel,
                displayOrder = displayOrder,
                iconName = iconName,
                description = description,
                menuType = menuType,
                createdBy = createdBy
            )
        }
    }
}

enum class MenuType(val description: String) {
    CATEGORY("카테고리"),
    MENU("메뉴"),
    FUNCTION("기능")
}
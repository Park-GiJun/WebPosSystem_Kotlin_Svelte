package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.MenuRepository
import com.gijun.backend.application.port.out.PermissionRepository
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.permission.entities.Menu
import com.gijun.backend.domain.permission.entities.Permission
import com.gijun.backend.domain.permission.entities.PermissionTargetType
import com.gijun.backend.domain.permission.entities.PermissionType
import com.gijun.backend.domain.user.entity.User
import com.gijun.backend.common.util.logger
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val menuRepository: MenuRepository,
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository
) {
    private val logger = logger()

    suspend fun getUserMenus(userId: String): List<UserMenuPermission> {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found: $userId")

        val allMenus = menuRepository.findActiveMenus()
        val userPermissions = getUserEffectivePermissions(user)

        return allMenus.mapNotNull { menu ->
            val permission = userPermissions.find { it.menuId.value == menu.menuId.value }
            permission?.let {
                UserMenuPermission(
                    menuId = menu.menuId.value,
                    menuCode = menu.menuCode,
                    menuName = menu.menuName,
                    menuPath = menu.menuPath,
                    parentMenuId = menu.parentMenuId?.value,
                    menuLevel = menu.menuLevel,
                    displayOrder = menu.displayOrder,
                    iconName = menu.iconName,
                    menuType = menu.menuType.name,
                    permissionType = it.permissionType.name,
                    hasReadPermission = hasPermission(it.permissionType, PermissionType.READ),
                    hasWritePermission = hasPermission(it.permissionType, PermissionType.WRITE),
                    hasDeletePermission = hasPermission(it.permissionType, PermissionType.DELETE),
                    hasAdminPermission = hasPermission(it.permissionType, PermissionType.ADMIN)
                )
            }
        }.sortedWith(
            compareBy<UserMenuPermission> { it.menuLevel }
                .thenBy { it.displayOrder }
                .thenBy { it.menuName }
        )
    }

    suspend fun checkUserPermission(userId: String, menuCode: String, requiredPermission: PermissionType): Boolean {
        val user = userRepository.findById(userId)
            ?: return false

        val menu = menuRepository.findByCode(menuCode)
            ?: return false

        val userPermissions = getUserEffectivePermissions(user)
        val menuPermission = userPermissions.find { it.menuId.value == menu.menuId.value }

        return menuPermission?.let {
            hasPermission(it.permissionType, requiredPermission)
        } ?: false
    }

    private suspend fun getUserEffectivePermissions(user: User): List<Permission> {
        val allPermissions = mutableListOf<Permission>()

        // 1. 사용자 직접 권한
        allPermissions.addAll(
            permissionRepository.findByTargetId(PermissionTargetType.USER, user.id)
        )

        // 2. 역할 기반 권한
        user.roles.forEach { role ->
            allPermissions.addAll(
                permissionRepository.findByTargetId(PermissionTargetType.ROLE, role.name)
            )
        }

        // 3. 매장 기반 권한 (사용자가 접근 권한이 있는 매장들)
        // TODO: 사용자-매장 권한 관계를 조회하여 매장 기반 권한 추가

        // 4. 본사 기반 권한 (사용자가 소속된 본사들)
        // TODO: 사용자-본사 관계를 조회하여 본사 기반 권한 추가

        // 활성 권한만 필터링하고 메뉴별로 최고 권한만 유지
        return allPermissions
            .filter { it.isValid() }
            .groupBy { it.menuId }
            .mapValues { (_, permissions) ->
                permissions.maxByOrNull { getPermissionLevel(it.permissionType) }!!
            }
            .values
            .toList()
    }

    private fun hasPermission(userPermission: PermissionType, requiredPermission: PermissionType): Boolean {
        return getPermissionLevel(userPermission) >= getPermissionLevel(requiredPermission)
    }

    private fun getPermissionLevel(permissionType: PermissionType): Int {
        return when (permissionType) {
            PermissionType.READ -> 1
            PermissionType.WRITE -> 2
            PermissionType.DELETE -> 3
            PermissionType.ADMIN -> 4
        }
    }

    suspend fun grantPermission(
        menuCode: String,
        targetType: PermissionTargetType,
        targetId: String,
        permissionType: PermissionType,
        grantedBy: String
    ): Permission {
        val menu = menuRepository.findByCode(menuCode)
            ?: throw IllegalArgumentException("Menu not found: $menuCode")

        val permission = when (targetType) {
            PermissionTargetType.USER -> {
                val user = userRepository.findById(targetId)
                    ?: throw IllegalArgumentException("User not found: $targetId")
                Permission.grantToUser(menu.menuId, com.gijun.backend.domain.user.vo.UserId.fromString(targetId), permissionType, grantedBy)
            }
            PermissionTargetType.ROLE -> {
                val role = com.gijun.backend.domain.user.enums.UserRole.valueOf(targetId)
                Permission.grantToRole(menu.menuId, role, permissionType, grantedBy)
            }
            PermissionTargetType.STORE -> {
                Permission.grantToStore(menu.menuId, com.gijun.backend.domain.store.vo.StoreId.fromString(targetId), permissionType, grantedBy)
            }
            PermissionTargetType.HEADQUARTERS -> {
                Permission.grantToHeadquarters(menu.menuId, com.gijun.backend.domain.store.vo.HeadquartersId.fromString(targetId), permissionType, grantedBy)
            }
        }

        return permissionRepository.save(permission)
    }

    suspend fun revokePermission(
        menuCode: String,
        targetType: PermissionTargetType,
        targetId: String
    ) {
        val menu = menuRepository.findByCode(menuCode)
            ?: throw IllegalArgumentException("Menu not found: $menuCode")

        permissionRepository.revokePermission(menu.menuId.value, targetType, targetId)
    }
}

data class UserMenuPermission(
    val menuId: String,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val parentMenuId: String?,
    val menuLevel: Int,
    val displayOrder: Int,
    val iconName: String?,
    val menuType: String,
    val permissionType: String,
    val hasReadPermission: Boolean,
    val hasWritePermission: Boolean,
    val hasDeletePermission: Boolean,
    val hasAdminPermission: Boolean
)
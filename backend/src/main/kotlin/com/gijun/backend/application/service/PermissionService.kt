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

    suspend fun getUserMenus(username: String): List<UserMenuPermission> {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found: $username")

        val allMenus = menuRepository.findActiveMenus()
        val userPermissions = getUserEffectivePermissions(user)

        // 사용자에게 권한이 있는 메뉴들
        val authorizedMenuIds = userPermissions.map { it.menuId.value }.toSet()
        
        // 권한이 있는 메뉴 + 해당 메뉴들의 부모 카테고리들도 포함
        val menusToShow = mutableSetOf<Menu>()
        
        // 1. 직접 권한이 있는 메뉴들 추가
        allMenus.filter { authorizedMenuIds.contains(it.menuId.value) }
            .forEach { menusToShow.add(it) }
        
        // 2. 권한이 있는 메뉴들의 모든 부모 카테고리들 추가
        fun addParentCategories(menu: Menu) {
            menu.parentMenuId?.let { parentId ->
                val parent = allMenus.find { it.menuId.value == parentId.value }
                if (parent != null && !menusToShow.contains(parent)) {
                    menusToShow.add(parent)
                    addParentCategories(parent) // 재귀적으로 상위 부모도 추가
                }
            }
        }
        
        menusToShow.toList().forEach { addParentCategories(it) }

        return menusToShow.map { menu ->
            val permission = userPermissions.find { it.menuId.value == menu.menuId.value }
            
            if (menu.menuType.name == "CATEGORY") {
                // 카테고리는 기본적으로 읽기 권한으로 처리
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
                    permissionType = "READ",
                    hasReadPermission = true,
                    hasWritePermission = false,
                    hasDeletePermission = false,
                    hasAdminPermission = false
                )
            } else {
                // 실제 메뉴는 권한 정보 사용
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
            }
        }.filterNotNull().sortedWith(
            compareBy<UserMenuPermission> { it.menuLevel }
                .thenBy { it.displayOrder }
                .thenBy { it.menuName }
        )
    }

    suspend fun getAllMenus(): List<MenuSummary> {
        val allMenus = menuRepository.findAll()
        
        return allMenus.map { menu ->
            MenuSummary(
                menuId = menu.menuId.value,
                menuCode = menu.menuCode,
                menuName = menu.menuName,
                menuPath = menu.menuPath,
                parentMenuId = menu.parentMenuId?.value,
                menuLevel = menu.menuLevel,
                displayOrder = menu.displayOrder,
                iconName = menu.iconName,
                menuType = menu.menuType.name,
                isActive = menu.isActive
            )
        }.sortedWith(
            compareBy<MenuSummary> { it.menuLevel }
                .thenBy { it.displayOrder }
                .thenBy { it.menuName }
        )
    }

    suspend fun checkUserPermission(username: String, menuCode: String, requiredPermission: PermissionType): Boolean {
        val user = userRepository.findByUsername(username)
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

    suspend fun getAllRolePermissions(): List<RolePermissionDto> {
        // 모든 역할별 권한 조회
        val allPermissions = permissionRepository.findActivePermissions()
        val allMenus = menuRepository.findAll()
        
        // 역할별로 그룹화
        val roleGroups = allPermissions
            .filter { it.targetType == PermissionTargetType.ROLE }
            .groupBy { it.targetId }
        
        return roleGroups.map { (roleName, permissions) ->
            val permissionSummaries = permissions.map { permission ->
                val menu = allMenus.find { it.menuId.value == permission.menuId.value }
                PermissionSummaryDto(
                    menuCode = menu?.menuCode ?: "UNKNOWN",
                    menuName = menu?.menuName ?: "Unknown Menu",
                    permissionType = permission.permissionType.name
                )
            }
            
            RolePermissionDto(
                roleName = roleName,
                roleDescription = getRoleDescription(roleName),
                permissions = permissionSummaries
            )
        }.sortedBy { it.roleName }
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.findById(userId)
    }

    suspend fun getUserDirectPermissions(userId: String): List<UserPermissionDetail> {
        val permissions = permissionRepository.findUserPermissions(userId)
        val allMenus = menuRepository.findAll()
        
        return permissions.map { permission ->
            val menu = allMenus.find { it.menuId.value == permission.menuId.value }
            UserPermissionDetail(
                menuCode = menu?.menuCode ?: "UNKNOWN",
                menuName = menu?.menuName ?: "Unknown Menu",
                permissionType = permission.permissionType.name,
                expiresAt = permission.expiresAt
            )
        }
    }

    suspend fun getUserRolePermissions(roleNames: List<String>): List<RolePermissionDetail> {
        val allPermissions = permissionRepository.findActivePermissions()
        val allMenus = menuRepository.findAll()
        
        return roleNames.flatMap { roleName ->
            val rolePermissions = allPermissions.filter { 
                it.targetType == PermissionTargetType.ROLE && it.targetId == roleName 
            }
            
            rolePermissions.map { permission ->
                val menu = allMenus.find { it.menuId.value == permission.menuId.value }
                RolePermissionDetail(
                    roleName = roleName,
                    menuCode = menu?.menuCode ?: "UNKNOWN",
                    menuName = menu?.menuName ?: "Unknown Menu",
                    permissionType = permission.permissionType.name,
                    expiresAt = permission.expiresAt
                )
            }
        }
    }

    private fun getRoleDescription(roleName: String): String {
        return when (roleName) {
            "SUPER_ADMIN" -> "최고 관리자"
            "SYSTEM_ADMIN" -> "시스템 관리자"
            "HQ_MANAGER" -> "본사 관리자"
            "STORE_MANAGER" -> "매장 관리자"
            "USER" -> "일반 사용자"
            else -> roleName
        }
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

    suspend fun getAllUsers(page: Int, size: Int, search: String?): List<User> {
        // TODO: UserRepository에 페이징과 검색 기능 추가 필요
        return userRepository.findAll()
            .let { users ->
                if (search.isNullOrBlank()) {
                    users
                } else {
                    users.filter { 
                        it.username.contains(search, ignoreCase = true) || 
                        it.email.contains(search, ignoreCase = true) 
                    }
                }
            }
            .drop(page * size)
            .take(size)
    }

    suspend fun getUserCount(search: String?): Long {
        // TODO: UserRepository에 카운트 기능 추가 필요
        return userRepository.findAll()
            .let { users ->
                if (search.isNullOrBlank()) {
                    users.size.toLong()
                } else {
                    users.filter { 
                        it.username.contains(search, ignoreCase = true) || 
                        it.email.contains(search, ignoreCase = true) 
                    }.size.toLong()
                }
            }
    }

    suspend fun getMenuPermissions(menuCode: String): List<MenuPermissionDetail> {
        val menu = menuRepository.findByCode(menuCode)
            ?: throw IllegalArgumentException("Menu not found: $menuCode")
        
        val permissions = permissionRepository.findByMenuId(menu.menuId.value)
        
        return permissions.map { permission ->
            MenuPermissionDetail(
                permissionId = permission.permissionId.value,
                targetType = permission.targetType.name,
                targetId = permission.targetId,
                permissionType = permission.permissionType.name,
                grantedBy = permission.grantedBy,
                grantedAt = permission.grantedAt,
                expiresAt = permission.expiresAt,
                isActive = permission.isValid()
            )
        }
    }

    suspend fun getMenuByCode(menuCode: String): MenuSummary? {
        val menu = menuRepository.findByCode(menuCode) ?: return null
        
        return MenuSummary(
            menuId = menu.menuId.value,
            menuCode = menu.menuCode,
            menuName = menu.menuName,
            menuPath = menu.menuPath,
            parentMenuId = menu.parentMenuId?.value,
            menuLevel = menu.menuLevel,
            displayOrder = menu.displayOrder,
            iconName = menu.iconName,
            menuType = menu.menuType.name,
            isActive = menu.isActive
        )
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

data class MenuSummary(
    val menuId: String,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val parentMenuId: String?,
    val menuLevel: Int,
    val displayOrder: Int,
    val iconName: String?,
    val menuType: String,
    val isActive: Boolean
)

data class UserPermissionDetail(
    val menuCode: String,
    val menuName: String,
    val permissionType: String,
    val expiresAt: java.time.LocalDateTime?
)

data class RolePermissionDetail(
    val roleName: String,
    val menuCode: String,
    val menuName: String,
    val permissionType: String,
    val expiresAt: java.time.LocalDateTime?
)

data class RolePermissionDto(
    val roleName: String,
    val roleDescription: String,
    val permissions: List<PermissionSummaryDto>
)

data class PermissionSummaryDto(
    val menuCode: String,
    val menuName: String,
    val permissionType: String
)

data class MenuPermissionDetail(
    val permissionId: String,
    val targetType: String,
    val targetId: String,
    val permissionType: String,
    val grantedBy: String,
    val grantedAt: java.time.LocalDateTime,
    val expiresAt: java.time.LocalDateTime?,
    val isActive: Boolean
)
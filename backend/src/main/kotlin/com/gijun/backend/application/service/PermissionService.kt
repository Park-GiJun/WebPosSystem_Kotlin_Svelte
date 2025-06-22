package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.MenuRepository
import com.gijun.backend.application.port.out.PermissionRepository
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.permission.entities.Menu
import com.gijun.backend.domain.permission.entities.Permission
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.common.util.logger
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val menuRepository: MenuRepository,
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository,
    private val permissionCacheRepository: com.gijun.backend.adapter.out.persistence.permission.PermissionCacheRepository
) {
    private val logger = logger()

    suspend fun getUserMenus(username: String): List<UserMenuPermission> {
        // 캐시에서 먼저 조회
        val cachedPermissions = permissionCacheRepository.getCachedUserMenuPermissions(username)
        if (cachedPermissions != null) {
            logger.debug("Retrieved user menu permissions from cache for user: $username")
            return cachedPermissions
        }

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

        val result = menusToShow.map { menu ->
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

        // 결과를 캐시에 저장
        permissionCacheRepository.cacheUserMenuPermissions(username, result)
        logger.debug("Cached user menu permissions for user: $username")

        return result
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
        // 임시로 admin 사용자는 모든 권한 허용 (디버깅용)
        if (username == "admin") {
            logger.debug("Admin user bypass: granting $requiredPermission access to $menuCode")
            return true
        }
        
        // 사용자 권한 요약을 캐시에서 먼저 확인
        val permissionSummary = permissionCacheRepository.getCachedUserPermissionSummary(username)
        if (permissionSummary != null) {
            val menuPermissionLevel = permissionSummary[menuCode] as? Int
            if (menuPermissionLevel != null) {
                val hasPermission = menuPermissionLevel >= getPermissionLevel(requiredPermission)
                logger.debug("Permission check from cache for user:$username, menu:$menuCode, required:${requiredPermission.name}, result:$hasPermission")
                return hasPermission
            }
        }

        val user = userRepository.findByUsername(username)
            ?: return false

        val menu = menuRepository.findByCode(menuCode)
            ?: return false

        val userPermissions = getUserEffectivePermissions(user)
        val menuPermission = userPermissions.find { it.menuId.value == menu.menuId.value }

        val result = menuPermission?.let {
            hasPermission(it.permissionType, requiredPermission)
        } ?: false

        // 권한 요약 정보 캐시 업데이트
        val currentSummary = permissionSummary?.toMutableMap() ?: mutableMapOf()
        currentSummary[menuCode] = menuPermission?.let { getPermissionLevel(it.permissionType) } ?: 0
        permissionCacheRepository.cacheUserPermissionSummary(username, currentSummary)

        return result
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

        // 3. 조직 기반 권한 (V13에서는 ORGANIZATION 타입으로 통합)
        if (user.organizationId != null) {
            allPermissions.addAll(
                permissionRepository.findByTargetId(PermissionTargetType.ORGANIZATION, user.organizationId)
            )
        }

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
                // 사용자 존재 확인
                userRepository.findById(targetId)
                    ?: throw IllegalArgumentException("User not found: $targetId")
                Permission.grantToUser(menu.menuId, com.gijun.backend.domain.user.vo.UserId.fromString(targetId), permissionType, grantedBy)
            }
            PermissionTargetType.ROLE -> {
                val role = com.gijun.backend.domain.user.enums.UserRole.valueOf(targetId)
                Permission.grantToRole(menu.menuId, role, permissionType, grantedBy)
            }
            PermissionTargetType.ORGANIZATION -> {
                Permission.grantToOrganization(menu.menuId, targetId, permissionType, grantedBy)
            }
        }

        val result = permissionRepository.save(permission)
        
        // 권한 변경 시 관련 캐시 무효화
        permissionCacheRepository.invalidatePermissionCaches(targetType.name, targetId)
        logger.info("Permission granted and related caches invalidated for ${targetType.name}:$targetId on menu:$menuCode")
        
        return result
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
                // Permission의 menuId.value는 실제로 menuCode입니다
                val menu = allMenus.find { it.menuCode == permission.menuId.value }
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
            // Permission의 menuId.value는 실제로 menuCode입니다
            val menu = allMenus.find { it.menuCode == permission.menuId.value }
            UserPermissionDetail(
                menuCode = menu?.menuCode ?: "UNKNOWN",
                menuName = menu?.menuName ?: "Unknown Menu",
                permissionType = permission.permissionType.name
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
                // Permission의 menuId.value는 실제로 menuCode입니다
                val menu = allMenus.find { it.menuCode == permission.menuId.value }
                RolePermissionDetail(
                    roleName = roleName,
                    menuCode = menu?.menuCode ?: "UNKNOWN",
                    menuName = menu?.menuName ?: "Unknown Menu",
                    permissionType = permission.permissionType.name
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
        
        // 권한 취소 시 관련 캐시 무효화
        permissionCacheRepository.invalidatePermissionCaches(targetType.name, targetId)
        logger.info("Permission revoked and related caches invalidated for ${targetType.name}:$targetId on menu:$menuCode")
    }

    suspend fun getAllUsers(page: Int, size: Int, search: String?): List<User> {
        return userRepository.findAllWithPaging(page, size, search)
    }

    suspend fun getUserCount(search: String?): Long {
        return userRepository.countWithSearch(search)
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
                createdAt = permission.createdAt,
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

    /**
     * 사용자 권한 캐시 강제 갱신
     */
    suspend fun refreshUserPermissionCache(username: String) {
        permissionCacheRepository.invalidateUserRelatedPermissions(username)
        logger.info("User permission cache refreshed for user: $username")
        
        // 새로운 권한 정보로 캐시 재생성
        getUserMenus(username)
    }

    /**
     * 모든 사용자 권한 캐시 강제 갱신 (시스템 레벨 변경 시)
     */
    suspend fun refreshAllPermissionCache() {
        permissionCacheRepository.invalidateAllUserMenuPermissions()
        permissionCacheRepository.invalidateMenuHierarchy()
        logger.info("All user permission caches have been refreshed")
    }

    /**
     * 특정 메뉴와 관련된 모든 캐시 갱신
     */
    suspend fun refreshMenuRelatedCache(menuCode: String) {
        permissionCacheRepository.invalidateMenuRelatedPermissions(menuCode)
        logger.info("Menu related caches refreshed for menu: $menuCode")
    }

    /**
     * 조직별 사용자 조회
     */
    suspend fun getUsersByOrganization(organizationId: String, organizationType: String? = null): List<User> {
        return if (organizationType != null) {
            userRepository.findByOrganizationIdAndType(organizationId, organizationType)
        } else {
            userRepository.findByOrganizationId(organizationId)
        }
    }

    /**
     * 역할별 사용자 조회
     */
    suspend fun getUsersByRole(roleName: String): List<User> {
        return userRepository.findByRole(roleName)
    }

    /**
     * 여러 역할에 해당하는 사용자 조회
     */
    suspend fun getUsersByRoles(roleNames: List<String>): List<User> {
        return userRepository.findByRoles(roleNames)
    }

    /**
     * 사용자의 조직 기반 권한 조회
     */
    suspend fun getUserOrganizationPermissions(username: String): List<OrganizationPermission> {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found: $username")

        val orgPermissions = mutableListOf<OrganizationPermission>()

        // 조직 권한 (V13에서는 ORGANIZATION 타입으로 통합)
        if (user.organizationId != null) {
            val permissions = permissionRepository.findByTargetId(PermissionTargetType.ORGANIZATION, user.organizationId)
            permissions.forEach { permission ->
                // Permission의 menuId.value는 실제로 menuCode입니다
                val menu = menuRepository.findByCode(permission.menuId.value)
                orgPermissions.add(
                    OrganizationPermission(
                        organizationType = user.organizationType ?: "UNKNOWN",
                        organizationId = user.organizationId,
                        menuCode = menu?.menuCode ?: "UNKNOWN",
                        menuName = menu?.menuName ?: "Unknown Menu",
                        permissionType = permission.permissionType.name,
                        createdAt = permission.createdAt
                    )
                )
            }
        }

        return orgPermissions
    }

    /**
     * 조직에 권한 부여
     */
    suspend fun grantOrganizationPermission(
        organizationType: String,
        organizationId: String,
        menuCode: String,
        permissionType: PermissionType,
        grantedBy: String
    ): Permission {
        val menu = menuRepository.findByCode(menuCode)
            ?: throw IllegalArgumentException("Menu not found: $menuCode")

        // V13에서는 모든 조직 타입이 ORGANIZATION으로 통합됨
        val permission = Permission.grantToOrganization(menu.menuId, organizationId, permissionType, grantedBy)

        val result = permissionRepository.save(permission)
        
        // 해당 조직 소속 모든 사용자의 캐시 무효화
        val orgUsers = getUsersByOrganization(organizationId, organizationType)
        orgUsers.forEach { user ->
            permissionCacheRepository.invalidateUserRelatedPermissions(user.username)
        }
        logger.info("Invalidated cache for ${orgUsers.size} users in organization $organizationType:$organizationId")
        
        return result
    }

    suspend fun getAllPermissionsList(): List<Map<String, Any>> {
        val allPermissions = permissionRepository.findActivePermissions()
        val allMenus = menuRepository.findAll()
        val allUsers = userRepository.findAll()
        
        logger.info("Found ${allPermissions.size} permissions and ${allMenus.size} menus")
        
        if (allMenus.isEmpty()) {
            logger.warn("No menus found in database!")
        } else {
            logger.info("First 5 menus: ${allMenus.take(5).map { "${it.menuId.value}:${it.menuCode}" }}")
        }
        
        if (allPermissions.isNotEmpty()) {
            logger.info("First 5 permissions: ${allPermissions.take(5).map { "${it.permissionId.value}:${it.menuId.value}" }}")
        }
        
        return allPermissions.mapIndexed { index, permission ->
            // Permission의 menuId.value는 실제로 menuCode입니다 (PermissionMapper 참조)
            val menu = allMenus.find { it.menuCode == permission.menuId.value }
            val user = if (permission.targetType == PermissionTargetType.USER) {
                allUsers.find { it.id == permission.targetId }
            } else null

            if (menu == null) {
                logger.warn("Menu not found for permission ${permission.permissionId.value} with menuCode ${permission.menuId.value}")
                logger.warn("Available menu codes: ${allMenus.map { it.menuCode }.take(10)}")
            } else if (index < 3) {
                logger.info("Found menu for permission ${permission.permissionId.value}: ${menu.menuCode} - ${menu.menuName}")
            }

            mapOf<String, Any>(
                "id" to permission.permissionId.value,
                "menuCode" to (menu?.menuCode ?: "UNKNOWN"),
                "menuName" to (menu?.menuName ?: "Unknown Menu"),
                "targetType" to permission.targetType.name,
                "targetId" to permission.targetId,
                "targetName" to when (permission.targetType) {
                    PermissionTargetType.USER -> user?.username ?: permission.targetId
                    PermissionTargetType.ROLE -> getRoleDescription(permission.targetId)
                    PermissionTargetType.ORGANIZATION -> permission.targetId
                },
                "permissionType" to permission.permissionType.name,
                "grantedBy" to (permission.grantedBy ?: "system"),
                "createdAt" to permission.createdAt.toString(),
                "isActive" to permission.isActive
            )
        }.sortedWith(
            compareBy<Map<String, Any>> { it["menuCode"] as String }
                .thenBy { it["targetType"] as String }
                .thenBy { it["targetId"] as String }
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
    val permissionType: String
)

data class RolePermissionDetail(
    val roleName: String,
    val menuCode: String,
    val menuName: String,
    val permissionType: String
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
    val grantedBy: String?,
    val createdAt: java.time.LocalDateTime,
    val isActive: Boolean
)

data class OrganizationPermission(
    val organizationType: String,
    val organizationId: String,
    val menuCode: String,
    val menuName: String,
    val permissionType: String,
    val createdAt: java.time.LocalDateTime
)

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.entities.PermissionTargetType
import com.gijun.backend.domain.permission.entities.PermissionType
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/permissions")
class AdminPermissionController(
    private val permissionService: PermissionService
) {

    @GetMapping("/menus")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getAllMenus(): ResponseEntity<MenuListResponse> {
        
        // TODO: 실제 메뉴 목록 조회 로직 구현
        val menus = listOf(
            MenuPermissionDto(
                menuId = "menu-admin",
                menuCode = "ADMIN",
                menuName = "슈퍼어드민",
                menuPath = "/admin",
                parentMenuId = null,
                menuLevel = 1,
                displayOrder = 10,
                iconName = "shield",
                menuType = "CATEGORY",
                isActive = true
            ),
            MenuPermissionDto(
                menuId = "menu-admin-users",
                menuCode = "ADMIN_USERS",
                menuName = "사용자 관리",
                menuPath = "/admin/users",
                parentMenuId = "menu-admin",
                menuLevel = 2,
                displayOrder = 10,
                iconName = "users",
                menuType = "MENU",
                isActive = true
            ),
            MenuPermissionDto(
                menuId = "menu-business",
                menuCode = "BUSINESS",
                menuName = "영업정보시스템",
                menuPath = "/business",
                parentMenuId = null,
                menuLevel = 1,
                displayOrder = 20,
                iconName = "building-office",
                menuType = "CATEGORY",
                isActive = true
            ),
            MenuPermissionDto(
                menuId = "menu-business-stores",
                menuCode = "BUSINESS_STORES",
                menuName = "매장 관리",
                menuPath = "/business/stores",
                parentMenuId = "menu-business",
                menuLevel = 2,
                displayOrder = 20,
                iconName = "building-storefront",
                menuType = "MENU",
                isActive = true
            )
        )
        
        return ResponseEntity.ok(MenuListResponse(menus))
    }

    @GetMapping("/users/{userId}/permissions")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getUserPermissions(
        @PathVariable userId: String
    ): ResponseEntity<UserPermissionResponse> {
        
        // TODO: 실제 사용자 권한 조회 로직 구현
        val permissions = listOf(
            UserPermissionDto(
                menuCode = "ADMIN_USERS",
                menuName = "사용자 관리",
                permissionType = "ADMIN",
                grantType = "ROLE",
                grantValue = "SUPER_ADMIN",
                expiresAt = null
            ),
            UserPermissionDto(
                menuCode = "BUSINESS_STORES",
                menuName = "매장 관리",
                permissionType = "READ",
                grantType = "USER",
                grantValue = userId,
                expiresAt = null
            )
        )
        
        return ResponseEntity.ok(UserPermissionResponse(userId, permissions))
    }

    @PostMapping("/grant")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.WRITE)
    suspend fun grantPermission(
        @Valid @RequestBody request: GrantPermissionRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 권한 부여 로직 구현
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/revoke")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.DELETE)
    suspend fun revokePermission(
        @Valid @RequestBody request: RevokePermissionRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 권한 회수 로직 구현
        return ResponseEntity.ok().build()
    }

    @GetMapping("/roles")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getRolePermissions(): ResponseEntity<List<RolePermissionDto>> {
        
        // TODO: 실제 역할별 권한 조회 로직 구현
        val rolePermissions = listOf(
            RolePermissionDto(
                roleName = "SUPER_ADMIN",
                roleDescription = "최고 관리자",
                permissions = listOf(
                    PermissionSummaryDto("ADMIN", "슈퍼어드민", "ADMIN"),
                    PermissionSummaryDto("BUSINESS", "영업정보시스템", "ADMIN"),
                    PermissionSummaryDto("POS", "POS시스템", "ADMIN")
                )
            ),
            RolePermissionDto(
                roleName = "HQ_MANAGER",
                roleDescription = "본사 관리자",
                permissions = listOf(
                    PermissionSummaryDto("BUSINESS", "영업정보시스템", "WRITE"),
                    PermissionSummaryDto("POS", "POS시스템", "READ")
                )
            )
        )
        
        return ResponseEntity.ok(rolePermissions)
    }
}

data class MenuListResponse(
    val menus: List<MenuPermissionDto>
)

data class MenuPermissionDto(
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

data class UserPermissionResponse(
    val userId: String,
    val permissions: List<UserPermissionDto>
)

data class UserPermissionDto(
    val menuCode: String,
    val menuName: String,
    val permissionType: String,
    val grantType: String, // USER, ROLE, STORE, HEADQUARTERS
    val grantValue: String,
    val expiresAt: java.time.LocalDateTime?
)

data class GrantPermissionRequest(
    val menuCode: String,
    val targetType: String, // USER, ROLE, STORE, HEADQUARTERS
    val targetId: String,
    val permissionType: String, // READ, WRITE, DELETE, ADMIN
    val expiresAt: java.time.LocalDateTime?
)

data class RevokePermissionRequest(
    val menuCode: String,
    val targetType: String,
    val targetId: String
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

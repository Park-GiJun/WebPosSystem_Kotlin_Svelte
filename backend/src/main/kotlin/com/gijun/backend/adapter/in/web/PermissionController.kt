package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.configuration.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/my-menus")
    suspend fun getMyMenus(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<MyMenusResponse> {
        val token = authorization.removePrefix("Bearer ")
        
        try {
            val username = jwtUtil.getUsernameFromToken(token)
            
            // 임시 메뉴 데이터 (실제로는 사용자 권한에 따라 동적으로 생성)
            val menus = listOf(
                MenuDto(
                    menuId = "menu-admin",
                    menuCode = "ADMIN",
                    menuName = "슈퍼어드민",
                    menuPath = "/admin",
                    parentMenuId = null,
                    menuLevel = 1,
                    displayOrder = 10,
                    iconName = "shield",
                    menuType = "CATEGORY"
                ),
                MenuDto(
                    menuId = "menu-admin-users",
                    menuCode = "ADMIN_USERS",
                    menuName = "사용자 관리",
                    menuPath = "/admin/users",
                    parentMenuId = "menu-admin",
                    menuLevel = 2,
                    displayOrder = 10,
                    iconName = "users",
                    menuType = "MENU"
                ),
                MenuDto(
                    menuId = "menu-admin-permissions",
                    menuCode = "ADMIN_PERMISSIONS",
                    menuName = "권한 관리",
                    menuPath = "/admin/permissions",
                    parentMenuId = "menu-admin",
                    menuLevel = 2,
                    displayOrder = 20,
                    iconName = "key",
                    menuType = "MENU"
                ),
                MenuDto(
                    menuId = "menu-business",
                    menuCode = "BUSINESS",
                    menuName = "영업정보시스템",
                    menuPath = "/business",
                    parentMenuId = null,
                    menuLevel = 1,
                    displayOrder = 20,
                    iconName = "building-office",
                    menuType = "CATEGORY"
                ),
                MenuDto(
                    menuId = "menu-business-stores",
                    menuCode = "BUSINESS_STORES",
                    menuName = "매장 관리",
                    menuPath = "/business/stores",
                    parentMenuId = "menu-business",
                    menuLevel = 2,
                    displayOrder = 20,
                    iconName = "building-storefront",
                    menuType = "MENU"
                ),
                MenuDto(
                    menuId = "menu-business-pos",
                    menuCode = "BUSINESS_POS",
                    menuName = "POS 관리",
                    menuPath = "/business/pos",
                    parentMenuId = "menu-business",
                    menuLevel = 2,
                    displayOrder = 30,
                    iconName = "computer-desktop",
                    menuType = "MENU"
                ),
                MenuDto(
                    menuId = "menu-pos",
                    menuCode = "POS",
                    menuName = "POS시스템",
                    menuPath = "/pos",
                    parentMenuId = null,
                    menuLevel = 1,
                    displayOrder = 30,
                    iconName = "computer-desktop",
                    menuType = "CATEGORY"
                ),
                MenuDto(
                    menuId = "menu-pos-sales",
                    menuCode = "POS_SALES",
                    menuName = "판매",
                    menuPath = "/pos/sales",
                    parentMenuId = "menu-pos",
                    menuLevel = 2,
                    displayOrder = 10,
                    iconName = "shopping-cart",
                    menuType = "MENU"
                )
            )

            val permissions = listOf(
                PermissionDto(
                    menuCode = "ADMIN_USERS",
                    permissionType = "ADMIN",
                    hasRead = true,
                    hasWrite = true,
                    hasDelete = true,
                    hasAdmin = true
                ),
                PermissionDto(
                    menuCode = "ADMIN_PERMISSIONS",
                    permissionType = "ADMIN",
                    hasRead = true,
                    hasWrite = true,
                    hasDelete = true,
                    hasAdmin = true
                ),
                PermissionDto(
                    menuCode = "BUSINESS_STORES",
                    permissionType = "WRITE",
                    hasRead = true,
                    hasWrite = true,
                    hasDelete = false,
                    hasAdmin = false
                ),
                PermissionDto(
                    menuCode = "BUSINESS_POS",
                    permissionType = "WRITE",
                    hasRead = true,
                    hasWrite = true,
                    hasDelete = false,
                    hasAdmin = false
                ),
                PermissionDto(
                    menuCode = "POS_SALES",
                    permissionType = "WRITE",
                    hasRead = true,
                    hasWrite = true,
                    hasDelete = false,
                    hasAdmin = false
                )
            )
            
            val response = MyMenusResponse(menus, permissions)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.status(401).build()
        }
    }

    @PostMapping("/check")
    suspend fun checkPermission(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: CheckPermissionRequest
    ): ResponseEntity<CheckPermissionResponse> {
        // 임시로 모든 권한을 허용
        return ResponseEntity.ok(CheckPermissionResponse(true))
    }
}

data class MyMenusResponse(
    val menus: List<MenuDto>,
    val permissions: List<PermissionDto>
)

data class MenuDto(
    val menuId: String,
    val menuCode: String,
    val menuName: String,
    val menuPath: String,
    val parentMenuId: String?,
    val menuLevel: Int,
    val displayOrder: Int,
    val iconName: String?,
    val menuType: String
)

data class PermissionDto(
    val menuCode: String,
    val permissionType: String,
    val hasRead: Boolean,
    val hasWrite: Boolean,
    val hasDelete: Boolean,
    val hasAdmin: Boolean
)

data class CheckPermissionRequest(
    val menuCode: String,
    val requiredPermission: String
)

data class CheckPermissionResponse(
    val hasPermission: Boolean
)

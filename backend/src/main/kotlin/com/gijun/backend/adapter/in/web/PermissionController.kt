package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.PermissionChecker
import com.gijun.backend.domain.permission.entities.PermissionType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val permissionService: PermissionService,
    private val permissionChecker: PermissionChecker
) {

    @GetMapping("/my-menus")
    suspend fun getMyMenus(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<MyMenusResponse> {
        val userId = permissionChecker.extractUserIdFromToken(authorization)
        
        val userMenus = permissionService.getUserMenus(userId)
        
        val response = MyMenusResponse(
            menus = userMenus.map { menu ->
                MenuDto(
                    menuId = menu.menuId,
                    menuCode = menu.menuCode,
                    menuName = menu.menuName,
                    menuPath = menu.menuPath,
                    parentMenuId = menu.parentMenuId,
                    menuLevel = menu.menuLevel,
                    displayOrder = menu.displayOrder,
                    iconName = menu.iconName,
                    menuType = menu.menuType
                )
            },
            permissions = userMenus.map { menu ->
                PermissionDto(
                    menuCode = menu.menuCode,
                    permissionType = menu.permissionType,
                    hasRead = menu.hasReadPermission,
                    hasWrite = menu.hasWritePermission,
                    hasDelete = menu.hasDeletePermission,
                    hasAdmin = menu.hasAdminPermission
                )
            }
        )
        
        return ResponseEntity.ok(response)
    }

    @PostMapping("/check")
    suspend fun checkPermission(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: CheckPermissionRequest
    ): ResponseEntity<CheckPermissionResponse> {
        val userId = permissionChecker.extractUserIdFromToken(authorization)
        
        val hasPermission = permissionService.checkUserPermission(
            userId, 
            request.menuCode, 
            PermissionType.valueOf(request.requiredPermission)
        )
        
        return ResponseEntity.ok(CheckPermissionResponse(hasPermission))
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
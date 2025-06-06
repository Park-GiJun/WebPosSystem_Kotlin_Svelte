package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/my-menus")
    suspend fun getMyMenus(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<MyMenusResponse> {
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        // 사용자 ID를 username에서 가져와야 하는데, 실제로는 UserRepository에서 조회해야 함
        // 여기서는 임시로 username을 ID로 사용
        val userMenus = permissionService.getUserMenus(username)
        
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
        val token = authorization.removePrefix("Bearer ")
        val username = jwtUtil.getUsernameFromToken(token)
        
        val hasPermission = permissionService.checkUserPermission(
            username, 
            request.menuCode, 
            com.gijun.backend.domain.permission.entities.PermissionType.valueOf(request.requiredPermission)
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
package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.permission.enums.PermissionType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val jwtUtil: JwtUtil,
    private val permissionService: PermissionService
) {

    @GetMapping("/my-menus")
    suspend fun getMyMenus(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<MyMenusResponse> {
        val token = authorization.removePrefix("Bearer ")
        
        try {
            val username = jwtUtil.getUsernameFromToken(token)
            
            // 실제 데이터베이스에서 사용자 메뉴 권한 조회
            val userMenuPermissions = permissionService.getUserMenus(username)
            
            val menus = userMenuPermissions.map { userMenuPermission ->
                MenuDto(
                    menuId = userMenuPermission.menuId,
                    menuCode = userMenuPermission.menuCode,
                    menuName = userMenuPermission.menuName,
                    menuPath = userMenuPermission.menuPath,
                    parentMenuId = userMenuPermission.parentMenuId,
                    menuLevel = userMenuPermission.menuLevel,
                    displayOrder = userMenuPermission.displayOrder,
                    iconName = userMenuPermission.iconName,
                    menuType = userMenuPermission.menuType
                )
            }

            val permissions = userMenuPermissions.map { userMenuPermission ->
                PermissionDto(
                    menuCode = userMenuPermission.menuCode,
                    permissionType = userMenuPermission.permissionType,
                    hasRead = userMenuPermission.hasReadPermission,
                    hasWrite = userMenuPermission.hasWritePermission,
                    hasDelete = userMenuPermission.hasDeletePermission,
                    hasAdmin = userMenuPermission.hasAdminPermission
                )
            }
            
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
        val token = authorization.removePrefix("Bearer ")
        
        try {
            val username = jwtUtil.getUsernameFromToken(token)
            val requiredPermission = PermissionType.valueOf(request.requiredPermission)
            
            val hasPermission = permissionService.checkUserPermission(
                username, 
                request.menuCode, 
                requiredPermission
            )
            
            return ResponseEntity.ok(CheckPermissionResponse(hasPermission))
        } catch (e: Exception) {
            return ResponseEntity.status(401).build()
        }
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

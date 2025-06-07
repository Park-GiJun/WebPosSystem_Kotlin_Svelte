package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.application.service.RolePermissionDto as ServiceRolePermissionDto
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.permission.enums.PermissionTargetType
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.common.util.logger
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/permissions")
class AdminPermissionController(
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {
    private val logger = logger()

    @GetMapping("/menus")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getAllMenus(): ResponseEntity<MenuListResponse> {
        
        // 실제 메뉴 목록 조회 로직 구현
        val menuService = permissionService.getAllMenus()
        
        val menus = menuService.map { menu ->
            MenuPermissionDto(
                menuId = menu.menuId,
                menuCode = menu.menuCode,
                menuName = menu.menuName,
                menuPath = menu.menuPath,
                parentMenuId = menu.parentMenuId,
                menuLevel = menu.menuLevel,
                displayOrder = menu.displayOrder,
                iconName = menu.iconName,
                menuType = menu.menuType,
                isActive = menu.isActive
            )
        }
        
        return ResponseEntity.ok(MenuListResponse(menus))
    }

    @GetMapping("/users/{userId}/permissions")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getUserPermissions(
        @PathVariable userId: String
    ): ResponseEntity<UserPermissionResponse> {
        
        return try {
            // 사용자 존재 확인
            val user = permissionService.getUserById(userId)
                ?: return ResponseEntity.notFound().build()
            
            // 사용자의 직접 권한 조회
            val directPermissions = permissionService.getUserDirectPermissions(userId)
            
            // 사용자의 역할 기반 권한 조회
            val rolePermissions = permissionService.getUserRolePermissions(user.roles.map { it.name })
            
            // 모든 권한을 통합하여 응답 생성
            val permissions = mutableListOf<UserPermissionDto>()
            
            // 직접 권한 추가
            directPermissions.forEach { permission ->
                permissions.add(
                    UserPermissionDto(
                        menuCode = permission.menuCode,
                        menuName = permission.menuName,
                        permissionType = permission.permissionType,
                        grantType = "USER",
                        grantValue = userId,
                        expiresAt = permission.expiresAt
                    )
                )
            }
            
            // 역할 기반 권한 추가
            rolePermissions.forEach { rolePermission ->
                permissions.add(
                    UserPermissionDto(
                        menuCode = rolePermission.menuCode,
                        menuName = rolePermission.menuName,
                        permissionType = rolePermission.permissionType,
                        grantType = "ROLE",
                        grantValue = rolePermission.roleName,
                        expiresAt = rolePermission.expiresAt
                    )
                )
            }
            
            ResponseEntity.ok(UserPermissionResponse(userId, permissions))
            
        } catch (e: Exception) {
            logger.error("Failed to get user permissions for userId: $userId", e)
            ResponseEntity.status(500).build()
        }
    }

    @PostMapping("/grant")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.WRITE)
    suspend fun grantPermission(
        @Valid @RequestBody request: GrantPermissionRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Map<String, Any>> {
        
        return try {
            val token = authorization.removePrefix("Bearer ")
            val grantedBy = jwtUtil.getUsernameFromToken(token)
            
            // 권한 부여
            val permission = permissionService.grantPermission(
                menuCode = request.menuCode,
                targetType = PermissionTargetType.valueOf(request.targetType),
                targetId = request.targetId,
                permissionType = PermissionType.valueOf(request.permissionType),
                grantedBy = grantedBy
            )
            
            val response = mapOf(
                "success" to true,
                "message" to "권한이 성공적으로 부여되었습니다.",
                "permissionId" to permission.permissionId.value,
                "grantedAt" to permission.grantedAt
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid permission grant request: ${e.message}")
            ResponseEntity.badRequest().body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "잘못된 요청입니다.")
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to grant permission", e)
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "message" to "권한 부여 중 오류가 발생했습니다."
                )
            )
        }
    }

    @DeleteMapping("/revoke")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.DELETE)
    suspend fun revokePermission(
        @Valid @RequestBody request: RevokePermissionRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Map<String, Any>> {
        
        return try {
            // 권한 회수
            permissionService.revokePermission(
                menuCode = request.menuCode,
                targetType = PermissionTargetType.valueOf(request.targetType),
                targetId = request.targetId
            )
            
            val response = mapOf(
                "success" to true,
                "message" to "권한이 성공적으로 회수되었습니다."
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid permission revoke request: ${e.message}")
            ResponseEntity.badRequest().body(
                mapOf(
                    "success" to false,
                    "message" to (e.message ?: "잘못된 요청입니다.")
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to revoke permission", e)
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "message" to "권한 회수 중 오류가 발생했습니다."
                )
            )
        }
    }

    @GetMapping("/roles")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getRolePermissions(): ResponseEntity<List<ServiceRolePermissionDto>> {
        
        return try {
            val rolePermissions = permissionService.getAllRolePermissions()
            ResponseEntity.ok(rolePermissions) 
            
        } catch (e: Exception) {
            logger.error("Failed to get role permissions", e)
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping("/users")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Map<String, Any>> {
        
        return try {
            val users = permissionService.getAllUsers(page, size, search)
            val totalCount = permissionService.getUserCount(search)
            
            val response = mapOf(
                "users" to users.map { user ->
                    mapOf(
                        "id" to user.id,
                        "username" to user.username,
                        "email" to user.email,
                        "roles" to user.roles.map { it.name },
                        "userStatus" to user.userStatus.name,
                        "lastLoginAt" to user.lastLoginAt,
                        "createdAt" to user.createdAt,
                        "isActive" to user.isActive
                    )
                },
                "totalCount" to totalCount,
                "page" to page,
                "size" to size,
                "totalPages" to ((totalCount + size - 1) / size)
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: Exception) {
            logger.error("Failed to get users", e)
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping("/menus/{menuCode}/permissions")
    @RequiresPermission(menuCode = "ADMIN_PERMISSIONS", permission = PermissionType.READ)
    suspend fun getMenuPermissions(
        @PathVariable menuCode: String
    ): ResponseEntity<Map<String, Any>> {
        
        return try {
            val menuPermissions = permissionService.getMenuPermissions(menuCode)
            val menu = permissionService.getMenuByCode(menuCode)
                ?: return ResponseEntity.notFound().build()
            
            val response = mapOf(
                "menu" to mapOf(
                    "menuId" to menu.menuId,
                    "menuCode" to menu.menuCode,
                    "menuName" to menu.menuName,
                    "menuPath" to menu.menuPath
                ),
                "permissions" to menuPermissions.map { permission ->
                    mapOf(
                        "permissionId" to permission.permissionId,
                        "targetType" to permission.targetType,
                        "targetId" to permission.targetId,
                        "permissionType" to permission.permissionType,
                        "grantedBy" to permission.grantedBy,
                        "grantedAt" to permission.grantedAt,
                        "expiresAt" to permission.expiresAt,
                        "isActive" to permission.isActive
                    )
                }
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: Exception) {
            logger.error("Failed to get menu permissions for menuCode: $menuCode", e)
            ResponseEntity.status(500).build()
        }
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

// RolePermissionDto와 PermissionSummaryDto는 PermissionService에서 가져와서 사용
// 중복 정의 제거

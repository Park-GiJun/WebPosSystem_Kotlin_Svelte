package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.adapter.out.persistence.permission.PermissionCacheRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/cache-debug")
class CacheDebugController(
    private val permissionService: PermissionService,
    private val permissionCacheRepository: PermissionCacheRepository
) {

    @DeleteMapping("/clear-user-cache/{username}")
    suspend fun clearUserCache(@PathVariable username: String): ResponseEntity<Map<String, Any>> {
        return try {
            // 사용자 관련 모든 캐시 삭제
            permissionCacheRepository.invalidateUserRelatedPermissions(username)
            
            // 권한 요약 캐시도 삭제
            permissionCacheRepository.invalidatePermissionCaches("USER", username)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "$username 사용자의 캐시가 삭제되었습니다."
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "message" to "캐시 삭제 중 오류: ${e.message}"
            ))
        }
    }
    
    @DeleteMapping("/clear-all-cache")
    suspend fun clearAllCache(): ResponseEntity<Map<String, Any>> {
        return try {
            // 모든 권한 캐시 삭제
            permissionCacheRepository.invalidateAllUserMenuPermissions()
            permissionCacheRepository.invalidateMenuHierarchy()
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "모든 권한 캐시가 삭제되었습니다."
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "message" to "캐시 삭제 중 오류: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/refresh-user-permissions/{username}")
    suspend fun refreshUserPermissions(@PathVariable username: String): ResponseEntity<Map<String, Any>> {
        return try {
            // 캐시 삭제 후 새로 로드
            permissionService.refreshUserPermissionCache(username)
            
            // 새로 로드된 메뉴 확인
            val userMenus = permissionService.getUserMenus(username)
            val adminMenus = userMenus.filter { it.menuCode.startsWith("ADMIN") }
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "$username 사용자의 권한이 갱신되었습니다.",
                "totalMenus" to userMenus.size,
                "adminMenus" to adminMenus.size,
                "adminMenuCodes" to adminMenus.map { it.menuCode },
                "hasAdminUsers" to adminMenus.any { it.menuCode == "ADMIN_USERS" }
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "message" to "권한 갱신 중 오류: ${e.message}"
            ))
        }
    }
    
    @GetMapping("/check-database-permissions/{username}")
    suspend fun checkDatabasePermissions(@PathVariable username: String): ResponseEntity<Map<String, Any>> {
        return try {
            // 사용자 직접 권한 확인
            val directPermissions = permissionService.getUserDirectPermissions(username)

            // 사용자 정보 확인
            val user = permissionService.getUserById(username)
            val userRoles = user?.roles?.map { it.name } ?: emptyList()

            // 역할 기반 권한 확인
            val rolePermissions = if (userRoles.isNotEmpty()) {
                permissionService.getUserRolePermissions(userRoles)
            } else {
                emptyList()
            }

            ResponseEntity.ok(mapOf(
                "username" to username,
                "userExists" to (user != null),
                "userRoles" to userRoles,
                "directPermissions" to directPermissions.map {
                    mapOf(
                        "menuCode" to it.menuCode,
                        "permissionType" to it.permissionType
                    )
                },
                "rolePermissions" to rolePermissions.map {
                    mapOf(
                        "roleName" to it.roleName,
                        "menuCode" to it.menuCode,
                        "permissionType" to it.permissionType
                    )
                },
                "totalDirectPermissions" to directPermissions.size,
                "totalRolePermissions" to rolePermissions.size
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "error" to e.message,
                "type" to e::class.simpleName
            ))
        } as ResponseEntity<Map<String, Any>>
    }
}

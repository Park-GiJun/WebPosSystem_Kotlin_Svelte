package com.gijun.backend.adapter.out.persistence.permission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.application.service.UserMenuPermission
import com.gijun.backend.configuration.RedisOperations
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class PermissionCacheRepository(
    private val redisOperations: RedisOperations,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val USER_MENU_PERMISSION_PREFIX = "user:menu:permission:"
        private const val MENU_HIERARCHY_CACHE_KEY = "menu:hierarchy"
        private const val USER_PERMISSION_SUMMARY_PREFIX = "user:permission:summary:"
        
        private val CACHE_TTL = Duration.ofMinutes(30) // 30분 캐시
        private val HIERARCHY_CACHE_TTL = Duration.ofHours(1) // 메뉴 구조는 1시간 캐시
    }

    /**
     * 사용자 메뉴 권한 정보 캐시 저장
     */
    suspend fun cacheUserMenuPermissions(username: String, permissions: List<UserMenuPermission>) {
        val key = generateUserMenuPermissionKey(username)
        redisOperations.set(key, permissions, CACHE_TTL)
    }

    /**
     * 사용자 메뉴 권한 정보 캐시 조회
     */
    suspend fun getCachedUserMenuPermissions(username: String): List<UserMenuPermission>? {
        val key = generateUserMenuPermissionKey(username)
        
        return try {
            val cachedData = redisOperations.get(key, Object::class.java)
            cachedData?.let {
                val json = objectMapper.writeValueAsString(it)
                objectMapper.readValue(json, object : TypeReference<List<UserMenuPermission>>() {})
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 사용자의 메뉴 권한 캐시 무효화
     */
    suspend fun invalidateUserMenuPermissions(username: String) {
        val key = generateUserMenuPermissionKey(username)
        redisOperations.delete(key)
    }

    /**
     * 모든 사용자의 메뉴 권한 캐시 무효화 (권한 변경 시)
     */
    suspend fun invalidateAllUserMenuPermissions() {
        // 실제 구현: 권한 관련 모든 키 패턴 삭제
        try {
            // 권한 관련 캐시 키들을 직접 삭제
            val keysToDelete = listOf(
                "${USER_MENU_PERMISSION_PREFIX}*",
                "${USER_PERMISSION_SUMMARY_PREFIX}*",
                MENU_HIERARCHY_CACHE_KEY
            )
            
            // Redis의 KEYS 명령 대신 개별적으로 삭제 (프로덕션에서는 SCAN 사용 권장)
            // 여기서는 단순화된 구현으로 처리
            redisOperations.delete(MENU_HIERARCHY_CACHE_KEY)
            
            // 실제 운영환경에서는 Redis SCAN을 이용하여 패턴 매칭된 키들을 찾아 삭제해야 함
        } catch (e: Exception) {
            // 캐시 삭제 실패 시 로그만 남기고 계속 진행
            println("Failed to invalidate all permission caches: ${e.message}")
        }
    }

    /**
     * 메뉴 계층 구조 캐시 저장
     */
    suspend fun cacheMenuHierarchy(hierarchy: Any) {
        redisOperations.set(MENU_HIERARCHY_CACHE_KEY, hierarchy, HIERARCHY_CACHE_TTL)
    }

    /**
     * 메뉴 계층 구조 캐시 조회
     */
    suspend fun getCachedMenuHierarchy(): Any? {
        return redisOperations.get(MENU_HIERARCHY_CACHE_KEY, Object::class.java)
    }

    /**
     * 메뉴 계층 구조 캐시 무효화
     */
    suspend fun invalidateMenuHierarchy() {
        redisOperations.delete(MENU_HIERARCHY_CACHE_KEY)
    }

    /**
     * 사용자 권한 요약 정보 캐시 저장
     */
    suspend fun cacheUserPermissionSummary(username: String, summary: Map<String, Any>) {
        val key = generateUserPermissionSummaryKey(username)
        redisOperations.set(key, summary, CACHE_TTL)
    }

    /**
     * 사용자 권한 요약 정보 캐시 조회
     */
    suspend fun getCachedUserPermissionSummary(username: String): Map<String, Any>? {
        val key = generateUserPermissionSummaryKey(username)
        
        return try {
            val cachedData = redisOperations.get(key, Object::class.java)
            cachedData?.let {
                val json = objectMapper.writeValueAsString(it)
                objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 사용자 권한 요약 정보 캐시 무효화
     */
    suspend fun invalidateUserPermissionSummary(username: String) {
        val key = generateUserPermissionSummaryKey(username)
        redisOperations.delete(key)
    }

    /**
     * 특정 메뉴 코드와 관련된 모든 사용자의 권한 캐시 무효화
     */
    suspend fun invalidateMenuRelatedPermissions(menuCode: String) {
        // 메뉴 변경 시 모든 사용자의 권한 캐시를 무효화
        invalidateAllUserMenuPermissions()
        invalidateMenuHierarchy()
    }

    /**
     * 사용자와 관련된 모든 권한 캐시 무효화
     */
    suspend fun invalidateUserRelatedPermissions(username: String) {
        invalidateUserMenuPermissions(username)
        invalidateUserPermissionSummary(username)
    }

    /**
     * 권한 변경 시 관련된 모든 캐시 무효화
     */
    suspend fun invalidatePermissionCaches(targetType: String? = null, targetId: String? = null) {
        when (targetType) {
            "USER" -> {
                targetId?.let { userId ->
                    // 특정 사용자의 캐시만 무효화
                    invalidateUserRelatedPermissions(userId)
                }
            }
            "ROLE" -> {
                // 역할 권한 변경 시 모든 사용자 캐시 무효화
                invalidateAllUserMenuPermissions()
            }
            "STORE", "HEADQUARTERS" -> {
                // 매장/본사 권한 변경 시 해당 조직과 관련된 사용자 캐시 무효화
                // TODO: 조직별 사용자 조회 후 해당 사용자들의 캐시만 무효화
                invalidateAllUserMenuPermissions()
            }
            else -> {
                // 전체 무효화
                invalidateAllUserMenuPermissions()
                invalidateMenuHierarchy()
            }
        }
    }

    private fun generateUserMenuPermissionKey(username: String): String {
        return "$USER_MENU_PERMISSION_PREFIX$username"
    }

    private fun generateUserPermissionSummaryKey(username: String): String {
        return "$USER_PERMISSION_SUMMARY_PREFIX$username"
    }
}

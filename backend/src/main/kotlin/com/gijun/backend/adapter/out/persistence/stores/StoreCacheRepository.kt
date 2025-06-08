package com.gijun.backend.adapter.out.persistence.stores

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.StoreId
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Store 캐시 레포지토리
 * Redis를 사용한 Store 정보 캐싱
 */
@Component
class StoreCacheRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    
    companion object {
        private const val STORE_CACHE_PREFIX = "store:"
        private const val STORE_LIST_CACHE_PREFIX = "store_list:"
        private val CACHE_TTL = Duration.ofHours(1) // 1시간 캐시
    }

    /**
     * 매장 정보 캐시 저장
     */
    suspend fun cacheStore(store: Store) {
        val key = generateStoreKey(store.storeId)
        val value = objectMapper.writeValueAsString(store)
        
        redisTemplate.opsForValue()
            .set(key, value, CACHE_TTL)
            .subscribe()
    }

    /**
     * 매장 정보 캐시 조회
     */
    suspend fun getCachedStore(storeId: StoreId): Store? {
        val key = generateStoreKey(storeId)
        
        return try {
            val value = redisTemplate.opsForValue()
                .get(key)
                .block()
                
            value?.let { 
                objectMapper.readValue(it, Store::class.java) 
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 매장 목록 캐시 저장
     */
    suspend fun cacheStoreList(cacheKey: String, stores: List<Store>) {
        val key = generateStoreListKey(cacheKey)
        val value = objectMapper.writeValueAsString(stores)
        
        redisTemplate.opsForValue()
            .set(key, value, CACHE_TTL)
            .subscribe()
    }

    /**
     * 매장 목록 캐시 조회
     */
    suspend fun getCachedStoreList(cacheKey: String): List<Store>? {
        val key = generateStoreListKey(cacheKey)
        
        return try {
            val value = redisTemplate.opsForValue()
                .get(key)
                .block()
                
            value?.let { 
                objectMapper.readValue(it, Array<Store>::class.java).toList()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 매장 캐시 삭제
     */
    suspend fun evictStore(storeId: StoreId) {
        val key = generateStoreKey(storeId)
        redisTemplate.delete(key).subscribe()
    }

    /**
     * 매장 목록 캐시 삭제
     */
    suspend fun evictStoreList(cacheKey: String) {
        val key = generateStoreListKey(cacheKey)
        redisTemplate.delete(key).subscribe()
    }

    /**
     * 매장 관련 모든 캐시 삭제
     */
    suspend fun evictAllStoreCaches() {
        val storePattern = "$STORE_CACHE_PREFIX*"
        val storeListPattern = "$STORE_LIST_CACHE_PREFIX*"
        
        // Store 캐시 삭제
        redisTemplate.keys(storePattern)
            .flatMap { redisTemplate.delete(it) }
            .subscribe()
            
        // Store List 캐시 삭제
        redisTemplate.keys(storeListPattern)
            .flatMap { redisTemplate.delete(it) }
            .subscribe()
    }

    /**
     * 특정 본사의 매장 캐시 삭제
     */
    suspend fun evictStoresByHq(hqId: String) {
        val pattern = "${STORE_CACHE_PREFIX}*hq:$hqId*"
        
        redisTemplate.keys(pattern)
            .flatMap { redisTemplate.delete(it) }
            .subscribe()
    }

    /**
     * 캐시 키 존재 확인
     */
    suspend fun existsInCache(storeId: StoreId): Boolean {
        val key = generateStoreKey(storeId)
        return redisTemplate.hasKey(key).block() ?: false
    }

    /**
     * 캐시 TTL 설정
     */
    suspend fun setCacheTtl(storeId: StoreId, ttl: Duration) {
        val key = generateStoreKey(storeId)
        redisTemplate.expire(key, ttl).subscribe()
    }

    // ===== Private Methods =====
    
    private fun generateStoreKey(storeId: StoreId): String {
        return "$STORE_CACHE_PREFIX${storeId.value}"
    }
    
    private fun generateStoreListKey(cacheKey: String): String {
        return "$STORE_LIST_CACHE_PREFIX$cacheKey"
    }
}
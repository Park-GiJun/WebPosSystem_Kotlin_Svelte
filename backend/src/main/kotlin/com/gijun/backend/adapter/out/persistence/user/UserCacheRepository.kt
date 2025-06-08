package com.gijun.backend.adapter.out.persistence.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.vo.UserId
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserCacheRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val USER_CACHE_PREFIX = "user:"
        private const val USER_LIST_CACHE_PREFIX = "user:list:"
        private val CACHE_TTL = Duration.ofHours(1)
    }

    /**
     * 유저 정보 캐시 저장
     */
    suspend fun cacheUser(user: User) {
        val key = generateUserKey(user.id)
        val value = objectMapper.writeValueAsString(user)

        redisTemplate.opsForValue()
            .set(key, value, CACHE_TTL)
            .subscribe()
    }

    suspend fun getCachedUser(id: String): User? {
        val key = generateUserKey(id)

        return try {
            val value = redisTemplate.opsForValue()
                .get(key)
                .block()

            value?.let {
                objectMapper.readValue(it, User::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun existsInCache(userId: String): Boolean {
        val key = generateUserKey(userId)
        return redisTemplate.hasKey(key).block() ?: false
    }

    suspend fun setCacheTtl(userId: String, ttl: Duration) {
        val key = generateUserKey(userId)
        redisTemplate.expire(key, ttl).subscribe()
    }

    private fun generateUserKey(userId: String): String {
        return "$USER_CACHE_PREFIX${userId}"
    }

    private fun generateUserListKey(cacheKey: String): String {
        return "$USER_LIST_CACHE_PREFIX$cacheKey"
    }
}
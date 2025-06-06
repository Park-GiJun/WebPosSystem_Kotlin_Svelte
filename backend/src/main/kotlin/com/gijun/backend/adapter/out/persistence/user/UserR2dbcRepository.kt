package com.gijun.backend.adapter.out.persistence.user

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserR2dbcRepository : CoroutineCrudRepository<UserEntity, String> {
    suspend fun findByUsername(username: String): UserEntity?
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun existsByUsername(username: String): Boolean
    suspend fun existsByEmail(email: String): Boolean

    // New methods for V2 features
    @Query("SELECT * FROM users WHERE user_status = :status AND is_active = true")
    suspend fun findByUserStatus(status: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE failed_login_attempts >= :threshold AND is_active = true")
    suspend fun findUsersWithFailedLogins(threshold: Int): List<UserEntity>

    @Query("SELECT * FROM users WHERE locked_until IS NOT NULL AND locked_until > NOW() AND is_active = true")
    suspend fun findLockedUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE email_verified_at IS NULL AND is_active = true")
    suspend fun findUnverifiedUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE last_login_at < :cutoffDate AND is_active = true")
    suspend fun findInactiveUsers(cutoffDate: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE JSON_CONTAINS(roles, :role) AND is_active = true")
    suspend fun findByRole(role: String): List<UserEntity>
}

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

    // Organization-related queries
    suspend fun findByOrganizationId(organizationId: String): List<UserEntity>
    suspend fun findByOrganizationType(organizationType: String): List<UserEntity>
    suspend fun findByOrganizationIdAndOrganizationType(organizationId: String, organizationType: String): List<UserEntity>

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

    // 페이징과 검색 기능 추가
    @Query("""
        SELECT * FROM users 
        WHERE (:search IS NULL OR 
               username LIKE CONCAT('%', :search, '%') OR 
               email LIKE CONCAT('%', :search, '%') OR
               first_name LIKE CONCAT('%', :search, '%') OR
               last_name LIKE CONCAT('%', :search, '%')) 
        AND is_active = true
        ORDER BY created_at DESC
        LIMIT :size OFFSET :offset
    """)
    suspend fun findAllWithPaging(search: String?, size: Int, offset: Int): List<UserEntity>

    @Query("""
        SELECT COUNT(*) FROM users 
        WHERE (:search IS NULL OR 
               username LIKE CONCAT('%', :search, '%') OR 
               email LIKE CONCAT('%', :search, '%') OR
               first_name LIKE CONCAT('%', :search, '%') OR
               last_name LIKE CONCAT('%', :search, '%'))
        AND is_active = true
    """)
    suspend fun countWithSearch(search: String?): Long

    // 여러 역할에 해당하는 사용자 조회
    @Query("""
        SELECT DISTINCT u.* FROM users u
        WHERE is_active = true 
        AND (
            JSON_CONTAINS(u.roles, JSON_QUOTE(:role1)) OR
            JSON_CONTAINS(u.roles, JSON_QUOTE(:role2)) OR
            JSON_CONTAINS(u.roles, JSON_QUOTE(:role3)) OR
            JSON_CONTAINS(u.roles, JSON_QUOTE(:role4)) OR
            JSON_CONTAINS(u.roles, JSON_QUOTE(:role5))
        )
    """)
    suspend fun findByRoles(role1: String?, role2: String?, role3: String?, role4: String?, role5: String?): List<UserEntity>

    // 특정 조직에서 특정 역할을 가진 사용자 조회
    @Query("""
        SELECT * FROM users 
        WHERE organization_id = :organizationId 
        AND JSON_CONTAINS(roles, JSON_QUOTE(:role))
        AND is_active = true
    """)
    suspend fun findByOrganizationIdAndRole(organizationId: String, role: String): List<UserEntity>
}

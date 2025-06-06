package com.gijun.backend.adapter.out.persistence.permission

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionR2dbcRepository : CoroutineCrudRepository<PermissionEntity, String> {
    
    @Query("SELECT * FROM permissions WHERE target_type = :targetType AND target_id = :targetId AND is_active = true AND (expires_at IS NULL OR expires_at > NOW())")
    fun findByTargetTypeAndTargetId(targetType: String, targetId: String): Flow<PermissionEntity>
    
    @Query("SELECT * FROM permissions WHERE menu_id = :menuId AND is_active = true AND (expires_at IS NULL OR expires_at > NOW())")
    fun findByMenuId(menuId: String): Flow<PermissionEntity>
    
    @Query("SELECT p.* FROM permissions p WHERE p.target_type = 'USER' AND p.target_id = :userId AND p.is_active = true AND (p.expires_at IS NULL OR p.expires_at > NOW())")
    fun findUserDirectPermissions(userId: String): Flow<PermissionEntity>
    
    @Query("SELECT * FROM permissions WHERE is_active = true AND (expires_at IS NULL OR expires_at > NOW())")
    fun findActivePermissions(): Flow<PermissionEntity>
    
    @Modifying
    @Query("UPDATE permissions SET is_active = false WHERE menu_id = :menuId AND target_type = :targetType AND target_id = :targetId")
    suspend fun revokePermission(menuId: String, targetType: String, targetId: String)
}
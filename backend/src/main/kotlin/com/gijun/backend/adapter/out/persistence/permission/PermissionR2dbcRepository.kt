package com.gijun.backend.adapter.out.persistence.permission

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionR2dbcRepository : CoroutineCrudRepository<PermissionEntity, String> {
    
    @Query("SELECT * FROM permissions WHERE permission_target_type = :targetType AND target_id = :targetId AND is_active = true")
    fun findByTargetTypeAndTargetId(targetType: String, targetId: String): Flow<PermissionEntity>
    
    @Query("SELECT * FROM permissions WHERE menu_code = :menuCode AND is_active = true")
    fun findByMenuCode(menuCode: String): Flow<PermissionEntity>
    
    @Query("SELECT p.* FROM permissions p WHERE p.permission_target_type = 'USER' AND p.target_id = :userId AND p.is_active = true")
    fun findUserDirectPermissions(userId: String): Flow<PermissionEntity>
    
    @Query("SELECT * FROM permissions WHERE is_active = true")
    fun findActivePermissions(): Flow<PermissionEntity>
    
    @Modifying
    @Query("UPDATE permissions SET is_active = false WHERE menu_code = :menuCode AND permission_target_type = :targetType AND target_id = :targetId")
    suspend fun revokePermission(menuCode: String, targetType: String, targetId: String)
}
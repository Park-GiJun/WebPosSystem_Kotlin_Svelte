package com.gijun.backend.adapter.out.persistence.permission

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuR2dbcRepository : CoroutineCrudRepository<MenuEntity, String> {
    suspend fun findByMenuCode(menuCode: String): MenuEntity?
    
    @Query("SELECT * FROM menus WHERE parent_menu_id = :parentMenuId AND is_active = true ORDER BY display_order")
    fun findByParentMenuId(parentMenuId: String?): Flow<MenuEntity>
    
    @Query("SELECT * FROM menus WHERE parent_menu_id IS NULL AND is_active = true ORDER BY display_order")
    fun findRootMenus(): Flow<MenuEntity>
    
    @Query("SELECT * FROM menus WHERE is_active = true ORDER BY menu_level, display_order")
    fun findActiveMenus(): Flow<MenuEntity>
    
    suspend fun existsByMenuCode(menuCode: String): Boolean
}
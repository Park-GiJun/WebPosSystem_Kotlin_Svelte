package com.gijun.backend.application.port.out

import com.gijun.backend.domain.permission.entities.Menu

interface MenuRepository {
    suspend fun save(menu: Menu): Menu
    suspend fun findById(menuId: String): Menu?
    suspend fun findByCode(menuCode: String): Menu?
    suspend fun findAll(): List<Menu>
    suspend fun findByParentMenuId(parentMenuId: String?): List<Menu>
    suspend fun findActiveMenus(): List<Menu>
    suspend fun deleteById(menuId: String)
}
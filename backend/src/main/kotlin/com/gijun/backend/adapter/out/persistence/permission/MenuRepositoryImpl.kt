package com.gijun.backend.adapter.out.persistence.permission

import com.gijun.backend.application.port.out.MenuRepository
import com.gijun.backend.domain.permission.entities.Menu
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class MenuRepositoryImpl(
    private val r2dbcRepository: MenuR2dbcRepository,
    private val menuMapper: MenuMapper
) : MenuRepository {

    override suspend fun save(menu: Menu): Menu {
        val entity = menuMapper.toEntity(menu)
        val savedEntity = r2dbcRepository.save(entity)
        return menuMapper.toDomain(savedEntity)
    }

    override suspend fun findById(menuId: String): Menu? {
        return r2dbcRepository.findById(menuId)?.let { menuMapper.toDomain(it) }
    }

    override suspend fun findByCode(menuCode: String): Menu? {
        return r2dbcRepository.findByMenuCode(menuCode)?.let { menuMapper.toDomain(it) }
    }

    override suspend fun findAll(): List<Menu> {
        return r2dbcRepository.findAll()
            .map { menuMapper.toDomain(it) }
            .toList()
    }

    override suspend fun findByParentMenuId(parentMenuId: String?): List<Menu> {
        return if (parentMenuId == null) {
            r2dbcRepository.findRootMenus()
                .map { menuMapper.toDomain(it) }
                .toList()
        } else {
            r2dbcRepository.findByParentMenuId(parentMenuId)
                .map { menuMapper.toDomain(it) }
                .toList()
        }
    }

    override suspend fun findActiveMenus(): List<Menu> {
        return r2dbcRepository.findActiveMenus()
            .map { menuMapper.toDomain(it) }
            .toList()
    }

    override suspend fun deleteById(menuId: String) {
        r2dbcRepository.deleteById(menuId)
    }
}
package com.gijun.backend.adapter.out.persistence.permission

import com.gijun.backend.application.port.out.PermissionRepository
import com.gijun.backend.domain.permission.entities.Permission
import com.gijun.backend.domain.permission.entities.PermissionTargetType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class PermissionRepositoryImpl(
    private val r2dbcRepository: PermissionR2dbcRepository,
    private val permissionMapper: PermissionMapper
) : PermissionRepository {

    override suspend fun save(permission: Permission): Permission {
        val entity = permissionMapper.toEntity(permission)
        val savedEntity = r2dbcRepository.save(entity)
        return permissionMapper.toDomain(savedEntity)
    }

    override suspend fun findById(permissionId: String): Permission? {
        return r2dbcRepository.findById(permissionId)?.let { permissionMapper.toDomain(it) }
    }

    override suspend fun findByTargetId(targetType: PermissionTargetType, targetId: String): List<Permission> {
        return r2dbcRepository.findByTargetTypeAndTargetId(targetType.name, targetId)
            .map { permissionMapper.toDomain(it) }
            .toList()
    }

    override suspend fun findByMenuId(menuId: String): List<Permission> {
        return r2dbcRepository.findByMenuId(menuId)
            .map { permissionMapper.toDomain(it) }
            .toList()
    }

    override suspend fun findUserPermissions(userId: String): List<Permission> {
        return r2dbcRepository.findUserDirectPermissions(userId)
            .map { permissionMapper.toDomain(it) }
            .toList()
    }

    override suspend fun findActivePermissions(): List<Permission> {
        return r2dbcRepository.findActivePermissions()
            .map { permissionMapper.toDomain(it) }
            .toList()
    }

    override suspend fun deleteById(permissionId: String) {
        r2dbcRepository.deleteById(permissionId)
    }

    override suspend fun revokePermission(menuId: String, targetType: PermissionTargetType, targetId: String) {
        r2dbcRepository.revokePermission(menuId, targetType.name, targetId)
    }
}
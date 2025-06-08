package com.gijun.backend.adapter.out.persistence.user

import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.user.entities.User
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val r2dbcRepository: UserR2dbcRepository,
    private val userMapper: UserMapper
) : UserRepository {

    override suspend fun save(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = r2dbcRepository.save(entity)
        return userMapper.toDomain(savedEntity)
    }

    override suspend fun findById(id: String): User? {
        return r2dbcRepository.findById(id)?.let { userMapper.toDomain(it) }
    }

    override suspend fun findByUsername(username: String): User? {
        return r2dbcRepository.findByUsername(username)?.let { userMapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: String): User? {
        return r2dbcRepository.findByEmail(email)?.let { userMapper.toDomain(it) }
    }

    override suspend fun findAll(): List<User> {
        return r2dbcRepository.findAll().toList().map { userMapper.toDomain(it) }
    }

    override suspend fun findByOrganizationId(organizationId: String): List<User> {
        return r2dbcRepository.findByOrganizationId(organizationId).map { userMapper.toDomain(it) }
    }

    override suspend fun findByOrganizationType(organizationType: String): List<User> {
        return r2dbcRepository.findByOrganizationType(organizationType).map { userMapper.toDomain(it) }
    }

    override suspend fun findByOrganizationIdAndType(organizationId: String, organizationType: String): List<User> {
        return r2dbcRepository.findByOrganizationIdAndOrganizationType(organizationId, organizationType).map { userMapper.toDomain(it) }
    }

    override suspend fun existsByUsername(username: String): Boolean {
        return r2dbcRepository.existsByUsername(username)
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return r2dbcRepository.existsByEmail(email)
    }

    override suspend fun deleteById(id: String) {
        r2dbcRepository.deleteById(id)
    }

    override suspend fun count(): Long {
        return r2dbcRepository.count()
    }

    override suspend fun findAllWithPaging(page: Int, size: Int, search: String?): List<User> {
        val offset = page * size
        return r2dbcRepository.findAllWithPaging(search, size, offset)
            .map { userMapper.toDomain(it) }
    }

    override suspend fun countWithSearch(search: String?): Long {
        return r2dbcRepository.countWithSearch(search)
    }

    override suspend fun findByRole(roleName: String): List<User> {
        return r2dbcRepository.findByRole("\"$roleName\"")
            .map { userMapper.toDomain(it) }
    }

    override suspend fun findByRoles(roleNames: List<String>): List<User> {
        // 최대 5개까지 역할을 지원 (더 많은 역할이 필요한 경우 다른 방식 사용)
        val roles = roleNames.take(5)
        val role1 = roles.getOrNull(0)
        val role2 = roles.getOrNull(1)
        val role3 = roles.getOrNull(2)
        val role4 = roles.getOrNull(3)
        val role5 = roles.getOrNull(4)
        
        return r2dbcRepository.findByRoles(role1, role2, role3, role4, role5)
            .map { userMapper.toDomain(it) }
    }
}

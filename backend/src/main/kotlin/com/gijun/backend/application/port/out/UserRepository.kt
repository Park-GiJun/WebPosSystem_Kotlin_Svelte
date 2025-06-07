package com.gijun.backend.application.port.out

import com.gijun.backend.domain.user.entity.User

interface UserRepository {

    suspend fun save(user: User): User

    suspend fun findById(id: String): User?

    suspend fun findByUsername(username: String): User?

    suspend fun findByEmail(email: String): User?

    suspend fun findAll(): List<User>

    suspend fun findByOrganizationId(organizationId: String): List<User>

    suspend fun findByOrganizationType(organizationType: String): List<User>

    suspend fun findByOrganizationIdAndType(organizationId: String, organizationType: String): List<User>

    suspend fun existsByUsername(username: String): Boolean

    suspend fun existsByEmail(email: String): Boolean

    suspend fun deleteById(id: String)

    suspend fun count(): Long
}

package com.gijun.backend.application.port.out

import com.gijun.backend.domain.user.entities.User

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

    // 페이징과 검색 기능 추가
    suspend fun findAllWithPaging(page: Int, size: Int, search: String? = null): List<User>

    suspend fun countWithSearch(search: String? = null): Long

    // 역할별 사용자 조회 기능 추가
    suspend fun findByRole(roleName: String): List<User>

    suspend fun findByRoles(roleNames: List<String>): List<User>
    
    // 프로시저 호출 메서드 추가
    suspend fun callGrantHeadquartersAdminPermissions(hqId: String, adminUserId: String)
    
    suspend fun callGrantStoreAdminPermissions(storeId: String, adminUserId: String, isChain: Boolean)
}

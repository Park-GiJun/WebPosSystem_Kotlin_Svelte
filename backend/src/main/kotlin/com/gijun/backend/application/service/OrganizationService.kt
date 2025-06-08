package com.gijun.backend.application.service

import com.gijun.backend.adapter.`in`.web.dto.organization.*
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class OrganizationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun createHeadquarters(request: CreateHeadquartersRequest, token: String): HeadquartersResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.ADMIN)) { "Only admin can create headquarters" }

        // 1. 본사 ID 생성
        val headquartersId = UUID.randomUUID().toString()
        
        // 2. 본사 관리자 계정 생성
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            username = request.adminUsername,
            email = request.email,
            passwordHash = passwordEncoder.encode("temp123!"), // 임시 패스워드
            roles = setOf(UserRole.HEADQUARTERS_ADMIN),
            organizationId = headquartersId,
            organizationType = "HEADQUARTERS",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        // TODO: HeadquartersRepository.save() 구현 필요
        val savedUser = userRepository.save(adminUser)

        return HeadquartersResponse(
            id = headquartersId,
            name = request.name,
            businessNumber = request.businessNumber,
            address = request.address,
            phoneNumber = request.phoneNumber,
            email = request.email,
            adminUser = AdminUserInfo(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    suspend fun createIndividualStore(request: CreateIndividualStoreRequest, token: String): StoreResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.ADMIN)) { "Only admin can create individual stores" }

        // 1. 개인매장 ID 생성
        val storeId = UUID.randomUUID().toString()
        
        // 2. 매장 관리자 계정 생성
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            username = request.ownerUsername,
            email = request.email,
            passwordHash = passwordEncoder.encode("temp123!"), // 임시 패스워드
            roles = setOf(UserRole.STORE_ADMIN),
            organizationId = storeId,
            organizationType = "INDIVIDUAL_STORE",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        // TODO: StoreRepository.save() 구현 필요
        val savedUser = userRepository.save(adminUser)

        return StoreResponse(
            id = storeId,
            name = request.name,
            businessNumber = request.businessNumber,
            address = request.address,
            phoneNumber = request.phoneNumber,
            email = request.email,
            storeType = "INDIVIDUAL",
            adminUser = AdminUserInfo(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            posCount = 1, // 기본 POS 1대
            isActive = true,
            createdAt = LocalDateTime.now()
        )
    }

    private suspend fun getCurrentUser(token: String): User {
        // TODO: JWT 토큰에서 사용자 정보 추출 및 조회
        // 임시로 admin 사용자 반환
        return userRepository.findByUsername("admin") 
            ?: throw IllegalStateException("Current user not found")
    }

    private fun User.hasRole(role: UserRole): Boolean {
        return this.roles.contains(role)
    }
}

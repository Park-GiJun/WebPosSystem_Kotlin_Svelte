package com.gijun.backend.application.service

import com.gijun.backend.adapter.`in`.web.dto.organization.*
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class OrganizationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun createHeadquarters(request: CreateHeadquartersRequest, token: String): HeadquartersResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.SUPER_ADMIN)) { "Only super admin can create headquarters" }

        // 1. 본사 생성
        val headquarters = Headquarters.create(
            hqCode = request.hqCode,
            hqName = request.hqName,
            createdBy = currentUser.username,
            businessLicense = request.businessLicense?.let { BusinessLicense.fromString(it) },
            ceoName = request.ceoName,
            headquartersAddress = request.address,
            contactPhone = request.contactPhone?.let { PhoneNumber.fromString(it) },
            website = request.website
        )

        // 2. 본사 관리자 계정 생성
        val adminUser = User(
            id = "HQ-${headquarters.hqId.value}-ADMIN",
            username = "${request.hqCode}_admin",
            email = "${request.hqCode}_admin@${request.hqCode.lowercase()}.webpos.com",
            passwordHash = passwordEncoder.encode(request.adminPassword),
            roles = setOf(UserRole.HQ_MANAGER),
            organizationId = headquarters.hqId.value,
            organizationType = "HEADQUARTERS",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        // TODO: HeadquartersRepository.save(headquarters)
        val savedUser = userRepository.save(adminUser)

        return HeadquartersResponse(
            hqId = headquarters.hqId.value,
            hqCode = headquarters.hqCode,
            hqName = headquarters.hqName,
            businessLicense = headquarters.businessLicense?.value,
            ceoName = headquarters.ceoName,
            address = headquarters.headquartersAddress,
            contactPhone = headquarters.contactPhone?.value,
            website = headquarters.website,
            adminUser = UserResponse(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            createdAt = headquarters.createdAt
        )
    }

    suspend fun createIndividualStore(request: CreateIndividualStoreRequest, token: String): StoreResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.SUPER_ADMIN)) { "Only super admin can create individual stores" }

        // 1. 개인매장 생성
        val store = Store.createIndividualStore(
            storeName = request.storeName,
            regionCode = request.regionCode,
            storeNumber = request.storeNumber,
            ownerName = request.ownerName,
            createdBy = currentUser.username,
            businessLicense = request.businessLicense?.let { BusinessLicense.fromString(it) },
            phoneNumber = request.phoneNumber?.let { PhoneNumber.fromString(it) },
            address = request.address,
            postalCode = request.postalCode,
            openingDate = request.openingDate ?: LocalDate.now()
        )

        // 2. 매장 관리자 계정 생성
        val adminUser = User(
            id = "ST-${store.storeId.value}-ADMIN",
            username = "IN_${request.regionCode}${request.storeNumber}_admin",
            email = "IN_${request.regionCode}${request.storeNumber}_admin@store.webpos.com",
            passwordHash = passwordEncoder.encode(request.adminPassword),
            roles = setOf(UserRole.STORE_MANAGER),
            organizationId = store.storeId.value,
            organizationType = "STORE",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        // TODO: StoreRepository.save(store)
        val savedUser = userRepository.save(adminUser)

        return StoreResponse(
            storeId = store.storeId.value,
            storeName = store.storeName,
            storeType = store.storeType.name,
            operationType = store.operationType?.name,
            regionCode = store.regionCode,
            storeNumber = store.storeNumber,
            ownerName = store.ownerName,
            businessLicense = store.businessLicense?.value,
            phoneNumber = store.phoneNumber?.value,
            address = store.address,
            postalCode = store.postalCode,
            openingDate = store.openingDate,
            storeStatus = store.storeStatus.name,
            managerUser = UserResponse(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            createdAt = store.createdAt
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

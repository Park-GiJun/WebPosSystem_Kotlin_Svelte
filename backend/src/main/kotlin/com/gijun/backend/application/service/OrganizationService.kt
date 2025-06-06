package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.entities.PosSystem
import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import com.gijun.backend.domain.user.entity.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OrganizationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
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
            username = request.adminUsername,
            email = request.adminEmail,
            passwordHash = passwordEncoder.encode(request.adminPassword),
            roles = setOf(UserRole.HQ_MANAGER),
            organizationId = headquarters.hqId.value,
            organizationType = "HEADQUARTERS",
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
            openingDate = request.openingDate
        )

        // 2. 매장 관리자 계정 생성
        val adminUser = User(
            username = request.adminUsername,
            email = request.adminEmail,
            passwordHash = passwordEncoder.encode(request.adminPassword),
            roles = setOf(UserRole.STORE_MANAGER),
            organizationId = store.storeId.value,
            organizationType = "STORE",
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
            businessLicense = store.businessLicense?.value,
            ownerName = store.ownerName,
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

    suspend fun createChainStore(request: CreateChainStoreRequest, token: String): StoreResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.HQ_MANAGER) && currentUser.organizationType == "HEADQUARTERS") { 
            "Only headquarters managers can create chain stores" 
        }

        val operationType = OperationType.valueOf(request.operationType)
        
        // 1. 체인매장 생성
        val store = Store.createChainStore(
            storeName = request.storeName,
            operationType = operationType,
            hqId = HeadquartersId.fromString(currentUser.organizationId!!),
            hqCode = extractHqCodeFromId(currentUser.organizationId!!),
            regionCode = request.regionCode,
            storeNumber = request.storeNumber,
            ownerName = request.ownerName,
            createdBy = currentUser.username,
            businessLicense = request.businessLicense?.let { BusinessLicense.fromString(it) },
            phoneNumber = request.phoneNumber?.let { PhoneNumber.fromString(it) },
            address = request.address,
            postalCode = request.postalCode,
            openingDate = request.openingDate
        )

        // 2. 매장 관리자 계정 생성
        val managerUser = User(
            username = request.managerUsername,
            email = request.managerEmail,
            passwordHash = passwordEncoder.encode(request.managerPassword),
            roles = setOf(UserRole.STORE_MANAGER),
            organizationId = store.storeId.value,
            organizationType = "STORE",
            createdBy = currentUser.id
        )

        // TODO: StoreRepository.save(store)
        val savedUser = userRepository.save(managerUser)

        return StoreResponse(
            storeId = store.storeId.value,
            storeName = store.storeName,
            storeType = store.storeType.name,
            operationType = store.operationType?.name,
            regionCode = store.regionCode,
            storeNumber = store.storeNumber,
            businessLicense = store.businessLicense?.value,
            ownerName = store.ownerName,
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

    suspend fun createStoreUser(request: CreateStoreUserRequest, token: String): UserResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.STORE_MANAGER) && currentUser.organizationType == "STORE") { 
            "Only store managers can create store users" 
        }

        val roles = request.roles.map { UserRole.valueOf(it) }.toSet()
        
        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            roles = roles,
            organizationId = currentUser.organizationId,
            organizationType = "STORE",
            createdBy = currentUser.id
        )

        val savedUser = userRepository.save(user)

        return UserResponse(
            id = savedUser.id,
            username = savedUser.username,
            email = savedUser.email,
            roles = savedUser.roles.map { it.name },
            userStatus = savedUser.userStatus.name
        )
    }

    suspend fun createPosSystem(request: CreatePosSystemRequest, token: String): PosSystemResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.STORE_MANAGER) && currentUser.organizationType == "STORE") { 
            "Only store managers can create POS systems" 
        }

        val posType = PosType.valueOf(request.posType)
        
        val posSystem = PosSystem.create(
            storeId = StoreId.fromString(currentUser.organizationId!!),
            posNumber = request.posNumber,
            posType = posType,
            posName = request.posName,
            createdBy = currentUser.username,
            ipAddress = request.ipAddress,
            macAddress = request.macAddress,
            serialNumber = request.serialNumber
        )

        // TODO: PosSystemRepository.save(posSystem)

        return PosSystemResponse(
            posId = posSystem.posId.value,
            storeId = posSystem.storeId.value,
            posNumber = posSystem.posNumber,
            posName = posSystem.posName,
            posType = posSystem.posType.name,
            ipAddress = posSystem.ipAddress,
            macAddress = posSystem.macAddress,
            serialNumber = posSystem.serialNumber,
            posStatus = posSystem.posStatus.name,
            createdAt = posSystem.createdAt
        )
    }

    suspend fun getAllHeadquarters(): List<HeadquartersResponse> {
        // TODO: HeadquartersRepository에서 조회
        return emptyList()
    }

    suspend fun getAllIndividualStores(): List<StoreResponse> {
        // TODO: StoreRepository에서 개인매장만 조회
        return emptyList()
    }

    suspend fun getStoresByHeadquarters(token: String): List<StoreResponse> {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasRole(UserRole.HQ_MANAGER) && currentUser.organizationType == "HEADQUARTERS") { 
            "Only headquarters managers can view their stores" 
        }

        // TODO: StoreRepository에서 해당 본사의 매장들 조회
        return emptyList()
    }

    private suspend fun getCurrentUser(token: String): User {
        val username = jwtUtil.getUsernameFromToken(token)
        return userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")
    }

    private fun extractHqCodeFromId(hqId: String): String {
        // HQ로 시작하는 ID에서 코드 부분 추출
        return hqId.removePrefix("HQ")
    }
}

// Request DTOs
data class CreateHeadquartersRequest(
    val hqCode: String,
    val hqName: String,
    val businessLicense: String? = null,
    val ceoName: String? = null,
    val address: String? = null,
    val contactPhone: String? = null,
    val website: String? = null,
    val adminUsername: String,
    val adminEmail: String,
    val adminPassword: String
)

data class CreateIndividualStoreRequest(
    val storeName: String,
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: String? = null,
    val ownerName: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val openingDate: LocalDate = LocalDate.now(),
    val adminUsername: String,
    val adminEmail: String,
    val adminPassword: String
)

data class CreateChainStoreRequest(
    val storeName: String,
    val operationType: String, // DIRECT, FRANCHISE
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: String? = null,
    val ownerName: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val openingDate: LocalDate = LocalDate.now(),
    val managerUsername: String,
    val managerEmail: String,
    val managerPassword: String
)

data class CreateStoreUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val roles: List<String> = listOf("USER")
)

data class CreatePosSystemRequest(
    val posNumber: Int,
    val posName: String? = null,
    val posType: String = "MAIN", // MAIN, SUB, MOBILE
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val serialNumber: String? = null
)

// Response DTOs
data class HeadquartersResponse(
    val hqId: String,
    val hqCode: String,
    val hqName: String,
    val businessLicense: String?,
    val ceoName: String?,
    val address: String?,
    val contactPhone: String?,
    val website: String?,
    val adminUser: UserResponse,
    val createdAt: java.time.LocalDateTime
)

data class StoreResponse(
    val storeId: String,
    val storeName: String,
    val storeType: String,
    val operationType: String?,
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: String?,
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: LocalDate,
    val storeStatus: String,
    val managerUser: UserResponse,
    val createdAt: java.time.LocalDateTime
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String
)

data class PosSystemResponse(
    val posId: String,
    val storeId: String,
    val posNumber: Int,
    val posName: String?,
    val posType: String,
    val ipAddress: String?,
    val macAddress: String?,
    val serialNumber: String?,
    val posStatus: String,
    val createdAt: java.time.LocalDateTime
)

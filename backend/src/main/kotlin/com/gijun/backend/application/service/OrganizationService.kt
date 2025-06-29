package com.gijun.backend.application.service

import com.gijun.backend.adapter.`in`.web.dto.organization.*
import com.gijun.backend.application.port.out.HeadquartersRepository
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.configuration.JwtUtil
import kotlinx.coroutines.flow.toList
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class OrganizationService(
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val headquartersRepository : HeadquartersRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    suspend fun createHeadquarters(request: CreateHeadquartersRequest, token: String): HeadquartersResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasAdminRole(UserRole.SUPER_ADMIN) || currentUser.hasAdminRole(UserRole.SYSTEM_ADMIN)) { 
            "Only admin can create headquarters" 
        }

        // 사용자명과 이메일 중복 체크
        val existingUserByUsername = userRepository.findByUsername(request.adminUsername)
        if (existingUserByUsername != null) {
            throw IllegalArgumentException("사용자명 '${request.adminUsername}'은 이미 사용 중입니다.")
        }
        
        val existingUserByEmail = userRepository.findByEmail(request.email)
        if (existingUserByEmail != null) {
            throw IllegalArgumentException("이메일 '${request.email}'은 이미 사용 중입니다.")
        }

        // 1. 본사 엔티티 생성
        val headquarters = Headquarters.create(
            hqCode = request.name.take(3).uppercase(), // 본사명에서 3글자 추출하여 코드로 사용
            hqName = request.name,
            businessLicense = BusinessLicense(request.businessNumber),
            headquartersAddress = request.address,
            contactPhone = request.phoneNumber?.let { PhoneNumber(it) },
            createdBy = currentUser.id
        )
        
        // 2. 본사 저장 (save가 Unit을 반환하므로 원본 엔티티 사용)
        headquartersRepository.save(headquarters)
        
        // 3. 본사 관리자 계정 생성
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            username = request.adminUsername,
            email = request.email,
            passwordHash = passwordEncoder.encode("temp123!"), // 임시 패스워드
            roles = setOf(UserRole.HQ_MANAGER), // HEADQUARTERS_ADMIN 대신 HQ_MANAGER 사용
            organizationId = headquarters.hqId.value,
            organizationType = "HEADQUARTERS",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        val savedUser = userRepository.save(adminUser)

        // 4. 프로시저 호출하여 권한 부여
        userRepository.callGrantHeadquartersAdminPermissions(headquarters.hqId.value, savedUser.id)

        return HeadquartersResponse(
            id = headquarters.hqId.value,
            name = headquarters.hqName,
            businessNumber = headquarters.businessLicense?.value ?: "",
            address = headquarters.headquartersAddress ?: "",
            phoneNumber = headquarters.contactPhone?.value,
            email = request.email, // 이메일은 관리자 계정의 이메일 사용
            adminUser = AdminUserInfo(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            createdAt = headquarters.createdAt,
            updatedAt = headquarters.updatedAt
        )
    }

    suspend fun createIndividualStore(request: CreateIndividualStoreRequest, token: String): StoreResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasAdminRole(UserRole.SUPER_ADMIN) || currentUser.hasAdminRole(UserRole.SYSTEM_ADMIN)) { 
            "Only admin can create individual stores" 
        }

        // 사용자명과 이메일 중복 체크
        val existingUserByUsername = userRepository.findByUsername(request.ownerUsername)
        if (existingUserByUsername != null) {
            throw IllegalArgumentException("사용자명 '${request.ownerUsername}'은 이미 사용 중입니다.")
        }
        
        val existingUserByEmail = userRepository.findByEmail(request.email)
        if (existingUserByEmail != null) {
            throw IllegalArgumentException("이메일 '${request.email}'은 이미 사용 중입니다.")
        }

        // 1. 개인매장 엔티티 생성
        val store = Store.createIndividualStore(
            storeName = request.name,
            regionCode = "001", // 기본 지역 코드 (서울)
            storeNumber = "001", // 기본 매장 번호
            ownerName = request.ownerUsername,
            businessLicense = BusinessLicense(request.businessNumber),
            phoneNumber = request.phoneNumber?.let { PhoneNumber(it) },
            address = request.address,
            createdBy = currentUser.id
        )
        
        // 2. 매장 저장
        val savedStore = storeRepository.save(store)
        
        // 3. 매장 관리자 계정 생성
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            username = request.ownerUsername,
            email = request.email,
            passwordHash = passwordEncoder.encode("temp123!"), // 임시 패스워드
            roles = setOf(UserRole.STORE_MANAGER), // STORE_ADMIN 대신 STORE_MANAGER 사용
            organizationId = savedStore.storeId.value,
            organizationType = "INDIVIDUAL_STORE",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        val savedUser = userRepository.save(adminUser)

        // 4. 프로시저 호출하여 권한 부여
        userRepository.callGrantStoreAdminPermissions(savedStore.storeId.value, savedUser.id, false)

        return StoreResponse(
            id = savedStore.storeId.value,
            name = savedStore.storeName,
            businessNumber = savedStore.businessLicense?.value ?: "",
            address = savedStore.address ?: "",
            phoneNumber = savedStore.phoneNumber?.value,
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
            isActive = savedStore.isActive,
            createdAt = savedStore.createdAt
        )
    }

    suspend fun getOrganizations(token: String): OrganizationsResponse {
        try {
            // 1. 본사 목록 조회 (실제 headquarters 테이블에서)
            val allHeadquarters = headquartersRepository.findAll()
            
            // 2. 본사별 매장 수와 사용자 수 계산
            val headquarters = allHeadquarters.map { hq ->
                // 해당 본사에 속한 사용자 수 계산
                val hqUserCount = userRepository.findByOrganizationId(hq.hqId.value).size
                
                HeadquartersSummaryDto(
                    id = hq.hqId.value,
                    name = hq.hqName,
                    businessNumber = hq.businessLicense?.value ?: "미등록",
                    storeCount = 0, // 체인점 수는 추후 구현
                    userCount = hqUserCount,
                    isActive = hq.isActive
                )
            }
            
            // 3. 개인매장 목록 조회
            val allStores = storeRepository.findAll().toList()
            val individualStores = allStores
                .filter { it.storeType == StoreType.INDIVIDUAL }
                .map { store ->
                    StoreSummaryDto(
                        id = store.storeId.value,
                        name = store.storeName,
                        businessNumber = store.businessLicense?.value ?: "미등록",
                        posCount = 1, // 기본 1대
                        isActive = store.isActive
                    )
                }
            
            // 4. 통계 계산
            val chainStores = allStores.filter { it.storeType == StoreType.CHAIN }
            val totalUsers = userRepository.count()
            
            val summary = OrganizationSummary(
                totalHeadquarters = headquarters.size,
                totalStores = chainStores.size,
                totalIndividualStores = individualStores.size,
                totalUsers = totalUsers.toInt()
            )
            
            return OrganizationsResponse(
                headquarters = headquarters,
                individualStores = individualStores,
                totalCount = headquarters.size + individualStores.size,
                summary = summary
            )
            
        } catch (e: Exception) {
            println("조직 목록 조회 중 오류 발생: ${e.message}")
            e.printStackTrace()
            
            return OrganizationsResponse(
                headquarters = emptyList(),
                individualStores = emptyList(),
                totalCount = 0,
                summary = OrganizationSummary(
                    totalHeadquarters = 0,
                    totalStores = 0,
                    totalIndividualStores = 0,
                    totalUsers = 1
                )
            )
        }
    }

    suspend fun deleteHeadquarters(headquartersId: String, token: String) {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasAdminRole(UserRole.SUPER_ADMIN) || currentUser.hasAdminRole(UserRole.SYSTEM_ADMIN)) { 
            "Only admin can delete headquarters" 
        }

        val hqId = HeadquartersId.fromString(headquartersId)
        
        // 1. 본사 존재 확인
        val headquarters = headquartersRepository.findByHqId(hqId)
            ?: throw IllegalArgumentException("본사를 찾을 수 없습니다: $headquartersId")

        // 2. 본사에 속한 사용자들 삭제 (소프트 삭제)
        val hqUsers = userRepository.findByOrganizationId(headquartersId)
        hqUsers.forEach { user ->
            userRepository.deleteById(user.id)
        }

        // 3. 본사 삭제
        headquartersRepository.deleteByHqId(hqId)
    }

    suspend fun deleteIndividualStore(storeId: String, token: String) {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasAdminRole(UserRole.SUPER_ADMIN) || currentUser.hasAdminRole(UserRole.SYSTEM_ADMIN)) { 
            "Only admin can delete individual stores" 
        }

        val storeIdVo = StoreId.fromString(storeId)
        
        // 1. 매장 존재 확인
        val store = storeRepository.findByStoreId(storeIdVo)
            ?: throw IllegalArgumentException("매장을 찾을 수 없습니다: $storeId")

        // 2. 매장에 속한 사용자들 삭제 (소프트 삭제)
        val storeUsers = userRepository.findByOrganizationId(storeId)
        storeUsers.forEach { user ->
            userRepository.deleteById(user.id)
        }

        // 3. 매장 삭제
        storeRepository.deleteByStoreId(storeIdVo)
    }

    private suspend fun getCurrentUser(token: String): User {
        val username = jwtUtil.getUsernameFromToken(token)
        return userRepository.findByUsername(username) 
            ?: throw IllegalStateException("Current user not found: $username")
    }

    private fun User.hasAdminRole(role: UserRole): Boolean {
        return this.roles.contains(role)
    }
    
    suspend fun createChainStore(headquartersId: String, request: CreateChainStoreRequest, token: String): StoreResponse {
        val currentUser = getCurrentUser(token)
        
        // 권한 체크: 시스템 관리자이거나 해당 본사의 관리자여야 함
        val isSystemAdmin = currentUser.hasAdminRole(UserRole.SUPER_ADMIN) || currentUser.hasAdminRole(UserRole.SYSTEM_ADMIN)
        val isHqAdmin = currentUser.organizationId == headquartersId && currentUser.hasAdminRole(UserRole.HQ_MANAGER)
        
        require(isSystemAdmin || isHqAdmin) { 
            "Only system admin or headquarters admin can create chain stores" 
        }
        
        // 본사 존재 확인
        val headquarters = headquartersRepository.findByHqId(HeadquartersId.fromString(headquartersId))
            ?: throw IllegalArgumentException("본사를 찾을 수 없습니다: $headquartersId")

        // 사용자명과 이메일 중복 체크
        val existingUserByUsername = userRepository.findByUsername(request.managerUsername)
        if (existingUserByUsername != null) {
            throw IllegalArgumentException("사용자명 '${request.managerUsername}'은 이미 사용 중입니다.")
        }
        
        val existingUserByEmail = userRepository.findByEmail(request.email)
        if (existingUserByEmail != null) {
            throw IllegalArgumentException("이메일 '${request.email}'은 이미 사용 중입니다.")
        }

        // 1. 체인점 엔티티 생성
        val store = Store.createChainStore(
            storeName = request.name,
            operationType = OperationType.DIRECT, // 기본적으로 직영점으로 설정
            hqId = HeadquartersId.fromString(headquartersId),
            hqCode = headquarters.hqCode,
            regionCode = request.regionCode,
            storeNumber = request.storeNumber,
            ownerName = request.managerUsername,
            createdBy = currentUser.id,
            businessLicense = request.businessNumber?.let { BusinessLicense(it) } ?: headquarters.businessLicense,
            phoneNumber = request.phoneNumber?.let { PhoneNumber(it) },
            address = request.address
        )
        
        // 2. 매장 저장
        val savedStore = storeRepository.save(store)
        
        // 3. 매장 관리자 계정 생성
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            username = request.managerUsername,
            email = request.email,
            passwordHash = passwordEncoder.encode("temp123!"), // 임시 패스워드
            roles = setOf(UserRole.STORE_MANAGER),
            organizationId = savedStore.storeId.value,
            organizationType = "CHAIN_STORE",
            userStatus = UserStatus.ACTIVE,
            createdBy = currentUser.id
        )

        val savedUser = userRepository.save(adminUser)
        
        // 4. 프로시저 호출하여 권한 부여
        userRepository.callGrantStoreAdminPermissions(savedStore.storeId.value, savedUser.id, true)

        return StoreResponse(
            id = savedStore.storeId.value,
            name = savedStore.storeName,
            businessNumber = savedStore.businessLicense?.value ?: "",
            address = savedStore.address ?: "",
            phoneNumber = savedStore.phoneNumber?.value,
            email = request.email,
            storeType = "CHAIN",
            adminUser = AdminUserInfo(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                roles = savedUser.roles.map { it.name },
                userStatus = savedUser.userStatus.name
            ),
            posCount = 1, // 기본 POS 1대
            isActive = savedStore.isActive,
            createdAt = savedStore.createdAt
        )
    }
}

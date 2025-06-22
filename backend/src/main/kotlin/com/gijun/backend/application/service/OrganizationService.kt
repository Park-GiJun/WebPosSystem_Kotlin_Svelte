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
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun createHeadquarters(request: CreateHeadquartersRequest, token: String): HeadquartersResponse {
        val currentUser = getCurrentUser(token)
        require(currentUser.hasAdminRole(UserRole.ADMIN) || currentUser.hasAdminRole(UserRole.SUPER_ADMIN)) { 
            "Only admin can create headquarters" 
        }

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
        require(currentUser.hasAdminRole(UserRole.ADMIN) || currentUser.hasAdminRole(UserRole.SUPER_ADMIN)) { 
            "Only admin can create individual stores" 
        }

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

    suspend fun getOrganizations(token: String): OrganizationsResponse {
        try {
            // 1. 모든 매장 목록 조회
            val allStores = storeRepository.findAll().toList()
            
            // 2. 본사 목록 생성 (체인점들을 본사별로 그룹핑)
            val headquarters = mutableListOf<HeadquartersSummaryDto>()
            val chainStores = allStores.filter { it.storeType == StoreType.CHAIN }
            
            // 본사별로 매장 그룹핑
            val storesByHq = chainStores.groupBy { it.hqId }
            
            storesByHq.forEach { (hqId, stores) ->
                if (hqId != null && stores.isNotEmpty()) {
                    val firstStore = stores.first()
                    // TODO: 실제 본사 정보는 별도 Headquarters 테이블에서 조회해야 함
                    headquarters.add(
                        HeadquartersSummaryDto(
                            id = hqId.value,
                            name = "${firstStore.storeName} 본사", // 임시 본사명
                            businessNumber = firstStore.businessLicense?.value ?: "미등록",
                            storeCount = stores.size,
                            userCount = stores.size * 3, // 임시 사용자 수 (매장당 평균 3명)
                            isActive = stores.any { it.isActive }
                        )
                    )
                }
            }
            
            // 3. 개인매장 목록 생성
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
            val totalStores = chainStores.size
            val totalUsers = userRepository.count() // count() 메서드 사용
            
            val summary = OrganizationSummary(
                totalHeadquarters = headquarters.size,
                totalStores = totalStores,
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

    private suspend fun getCurrentUser(token: String): User {
        // TODO: JWT 토큰 파싱 구현 필요
        return userRepository.findByUsername("admin") 
            ?: throw IllegalStateException("Current user not found")
    }

    private fun User.hasAdminRole(role: UserRole): Boolean {
        return this.roles.contains(role)
    }
}

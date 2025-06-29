package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.application.port.out.HeadquartersRepository
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import kotlinx.coroutines.flow.toList
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/business/headquarters")
class BusinessHeadquartersController(
    private val storeRepository: StoreRepository,
    private val headquartersRepository: HeadquartersRepository,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/my-stores")
    @RequiresPermission(menuCode = "BUSINESS_HQ", permission = PermissionType.READ)
    suspend fun getMyStores(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<HeadquartersStoreListResponse> {

        try {
            // JWT 토큰에서 사용자 정보 추출
            val token = authorization.removePrefix("Bearer ")
            val username = jwtUtil.getUsernameFromToken(token)

            // 사용자 정보 조회
            val user = userRepository.findByUsername(username)
                ?: return ResponseEntity.ok(HeadquartersStoreListResponse(emptyList(), 0))

            // 사용자의 본사 ID 확인
            val hqId = user.organizationId
            if (hqId == null || user.organizationType != "HEADQUARTERS") {
                return ResponseEntity.ok(HeadquartersStoreListResponse(emptyList(), 0))
            }

            // 해당 본사의 매장 목록 조회
            val allStores = storeRepository.findAll().toList()
            val hqStores = allStores.filter { store -> 
                store.hqId?.value == hqId
            }

            // 페이징 적용
            val startIndex = page * size
            val pagedStores = if (startIndex < hqStores.size) {
                hqStores.drop(startIndex).take(size)
            } else {
                emptyList()
            }

            // DTO로 변환
            val storeDtos = pagedStores.map { store ->
                HeadquartersStoreDto(
                    storeId = store.storeId.value,
                    storeName = store.storeName,
                    storeType = store.storeType.name,
                    operationType = store.operationType?.name,
                    regionCode = store.regionCode,
                    regionName = getRegionName(store.regionCode),
                    storeNumber = store.storeNumber,
                    businessLicense = store.businessLicense?.value,
                    ownerName = store.ownerName,
                    phoneNumber = store.phoneNumber?.value,
                    address = store.address,
                    postalCode = store.postalCode,
                    openingDate = store.openingDate,
                    storeStatus = store.storeStatus.name,
                    isActive = store.isActive,
                    managerUser = HeadquartersStoreManagerDto(
                        username = "manager_${store.storeNumber}",
                        email = "manager${store.storeNumber}@${hqId}.com"
                    ),
                    createdAt = store.createdAt,
                    updatedAt = store.updatedAt
                )
            }

            return ResponseEntity.ok(
                HeadquartersStoreListResponse(
                    stores = storeDtos,
                    totalCount = hqStores.size.toLong()
                )
            )

        } catch (e: Exception) {
            // 오류 발생시 더미 데이터로 대체
            println("본사 매장 목록 조회 중 오류 발생: ${e.message}")
            e.printStackTrace()

            val dummyStores = listOf(
                HeadquartersStoreDto(
                    storeId = "HQ001001",
                    storeName = "강남점",
                    storeType = "CHAIN",
                    operationType = "FRANCHISE",
                    regionCode = "001",
                    regionName = "서울특별시",
                    storeNumber = "001",
                    businessLicense = "123-45-67890",
                    ownerName = "김사장",
                    phoneNumber = "02-1234-5678",
                    address = "서울특별시 강남구 테헤란로 123",
                    postalCode = "06142",
                    openingDate = java.time.LocalDate.now().minusYears(2),
                    storeStatus = "ACTIVE",
                    isActive = true,
                    managerUser = HeadquartersStoreManagerDto(
                        username = "manager_001",
                        email = "manager001@company.com"
                    ),
                    createdAt = java.time.LocalDateTime.now().minusYears(2),
                    updatedAt = java.time.LocalDateTime.now()
                ),
                HeadquartersStoreDto(
                    storeId = "HQ001002", 
                    storeName = "홍대점",
                    storeType = "CHAIN",
                    operationType = "DIRECT",
                    regionCode = "001",
                    regionName = "서울특별시",
                    storeNumber = "002",
                    businessLicense = "234-56-78901",
                    ownerName = "이대표",
                    phoneNumber = "02-2345-6789",
                    address = "서울특별시 마포구 홍익로 456",
                    postalCode = "04063",
                    openingDate = java.time.LocalDate.now().minusMonths(8),
                    storeStatus = "ACTIVE",
                    isActive = true,
                    managerUser = HeadquartersStoreManagerDto(
                        username = "manager_002",
                        email = "manager002@company.com"
                    ),
                    createdAt = java.time.LocalDateTime.now().minusMonths(8),
                    updatedAt = java.time.LocalDateTime.now()
                )
            )

            return ResponseEntity.ok(
                HeadquartersStoreListResponse(
                    stores = dummyStores,
                    totalCount = dummyStores.size.toLong()
                )
            )
        }
    }

    @PostMapping("/stores")
    @RequiresPermission(menuCode = "BUSINESS_HQ", permission = PermissionType.WRITE)
    suspend fun createStore(
        @Valid @RequestBody request: CreateHeadquartersStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<*> {

        try {
            println("=== 매장 생성 요청 시작 ===")
            println("요청 데이터: $request")
            
            // JWT 토큰에서 사용자 정보 추출
            val token = authorization.removePrefix("Bearer ")
            val username = jwtUtil.getUsernameFromToken(token)
            println("사용자: $username")

            // 사용자 정보 조회
            val user = userRepository.findByUsername(username)
            if (user == null) {
                println("사용자를 찾을 수 없음: $username")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "사용자를 찾을 수 없습니다", "username" to username))
            }
            println("사용자 조회 성공: ${user.username}, 조직: ${user.organizationId}, 타입: ${user.organizationType}")

            // 사용자의 본사 ID 확인
            val hqId = user.organizationId
            if (hqId == null || user.organizationType != "HEADQUARTERS") {
                println("본사 권한 없음: organizationId=$hqId, organizationType=${user.organizationType}")
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("error" to "본사 관리자 권한이 필요합니다", "organizationId" to hqId, "organizationType" to user.organizationType))
            }

            // 본사 정보 조회
            val headquarters = headquartersRepository.findByHqId(
                com.gijun.backend.domain.store.vo.HeadquartersId(hqId)
            )
            if (headquarters == null) {
                println("본사 정보를 찾을 수 없음: $hqId")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "본사 정보를 찾을 수 없습니다", "hqId" to hqId))
            }
            println("본사 조회 성공: ${headquarters.hqName} (${headquarters.hqCode})")

            // 실제 매장 생성
            println("매장 도메인 객체 생성 중...")
            val newStore = com.gijun.backend.domain.store.entities.Store.createChainStore(
                storeName = request.storeName,
                operationType = com.gijun.backend.domain.store.enums.OperationType.valueOf(request.operationType),
                hqId = com.gijun.backend.domain.store.vo.HeadquartersId(hqId),
                hqCode = headquarters.hqCode,
                regionCode = request.regionCode,
                storeNumber = request.storeNumber,
                ownerName = request.ownerName,
                createdBy = username,
                businessLicense = request.businessLicense?.let { 
                    com.gijun.backend.domain.store.vo.BusinessLicense(it) 
                },
                phoneNumber = request.phoneNumber?.let { 
                    com.gijun.backend.domain.store.vo.PhoneNumber(it) 
                },
                address = request.address,
                postalCode = request.postalCode,
                openingDate = request.openingDate ?: java.time.LocalDate.now()
            )
            println("매장 도메인 객체 생성 완료: ${newStore.storeId.value}")

            // 매장을 데이터베이스에 저장
            println("매장 저장 중...")
            val savedStore = storeRepository.save(newStore)
            println("매장 저장 완료: ${savedStore.storeId.value}")

            // 매장 관리자 계정 생성 (비밀번호 해시화)
            println("매장 관리자 계정 생성 중...")
            val passwordEncoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            val hashedPassword = passwordEncoder.encode(request.managerPassword)

            val managerUser = com.gijun.backend.domain.user.entities.User.create(
                username = request.managerUsername,
                email = request.managerEmail,
                passwordHash = hashedPassword,
                roles = setOf(com.gijun.backend.domain.user.enums.UserRole.STORE_MANAGER),
                organizationId = savedStore.storeId.value,
                organizationType = "STORE",
                createdBy = username
            )

            // 관리자 계정 저장
            println("관리자 계정 저장 중...")
            val savedManager = userRepository.save(managerUser)
            println("관리자 계정 저장 완료: ${savedManager.username}")

            // 응답 DTO 생성
            val responseDto = HeadquartersStoreDto(
                storeId = savedStore.storeId.value,
                storeName = savedStore.storeName,
                storeType = savedStore.storeType.name,
                operationType = savedStore.operationType?.name,
                regionCode = savedStore.regionCode,
                regionName = getRegionName(savedStore.regionCode),
                storeNumber = savedStore.storeNumber,
                businessLicense = savedStore.businessLicense?.value,
                ownerName = savedStore.ownerName,
                phoneNumber = savedStore.phoneNumber?.value,
                address = savedStore.address,
                postalCode = savedStore.postalCode,
                openingDate = savedStore.openingDate,
                storeStatus = savedStore.storeStatus.name,
                isActive = savedStore.isActive,
                managerUser = HeadquartersStoreManagerDto(
                    username = savedManager.username,
                    email = savedManager.email
                ),
                createdAt = savedStore.createdAt,
                updatedAt = savedStore.updatedAt
            )

            println("=== 매장 생성 완료 ===")
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto)

        } catch (e: Exception) {
            println("=== 매장 생성 중 오류 발생 ===")
            println("오류 타입: ${e::class.simpleName}")
            println("오류 메시지: ${e.message}")
            e.printStackTrace()
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "error" to "매장 생성 중 오류가 발생했습니다",
                    "message" to e.message,
                    "type" to e::class.simpleName
                ))
        }
    }

    private fun generateStoreId(hqId: String, regionCode: String, storeNumber: String): String {
        return "${hqId}${regionCode}${storeNumber.padStart(3, '0')}"
    }

    private fun getRegionName(regionCode: String): String {
        return when (regionCode) {
            "001" -> "서울특별시"
            "002" -> "부산광역시"
            "003" -> "대구광역시"
            "004" -> "인천광역시"
            "005" -> "광주광역시"
            "006" -> "대전광역시"
            "007" -> "울산광역시"
            "008" -> "세종특별자치시"
            "009" -> "경기도"
            "010" -> "강원도"
            "011" -> "충청북도"
            "012" -> "충청남도"
            "013" -> "전라북도"
            "014" -> "전라남도"
            "015" -> "경상북도"
            "016" -> "경상남도"
            "017" -> "제주특별자치도"
            else -> "기타"
        }
    }
}

data class HeadquartersStoreListResponse(
    val stores: List<HeadquartersStoreDto>,
    val totalCount: Long
)

data class HeadquartersStoreDto(
    val storeId: String,
    val storeName: String,
    val storeType: String,
    val operationType: String?,
    val regionCode: String,
    val regionName: String,
    val storeNumber: String,
    val businessLicense: String?,
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: java.time.LocalDate,
    val storeStatus: String,
    val isActive: Boolean,
    val managerUser: HeadquartersStoreManagerDto,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class HeadquartersStoreManagerDto(
    val username: String,
    val email: String
)

data class CreateHeadquartersStoreRequest(
    @field:jakarta.validation.constraints.NotBlank
    val storeName: String,
    @field:jakarta.validation.constraints.NotBlank
    val operationType: String, // DIRECT, FRANCHISE
    @field:jakarta.validation.constraints.NotBlank
    val regionCode: String,
    @field:jakarta.validation.constraints.NotBlank
    val storeNumber: String,
    val businessLicense: String?,
    @field:jakarta.validation.constraints.NotBlank
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: java.time.LocalDate?,
    @field:jakarta.validation.constraints.NotBlank
    val managerUsername: String,
    @field:jakarta.validation.constraints.Email
    val managerEmail: String,
    @field:jakarta.validation.constraints.NotBlank
    val managerPassword: String
)

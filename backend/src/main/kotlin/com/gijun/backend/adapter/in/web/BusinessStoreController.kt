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
@RequestMapping("/api/v1/business/stores")
class BusinessStoreController(
    private val storeRepository: StoreRepository,
    private val headquartersRepository: HeadquartersRepository,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    @GetMapping
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getStores(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<StoreListResponse> {

        try {
            // 실제 데이터베이스에서 매장 목록 조회
            val allStores = storeRepository.findAll().toList()

            // 필터링 적용
            val filteredStores = allStores.filter { store ->
                val matchesStatus = status == null || store.storeStatus.name == status
                val matchesType = type == null || store.storeType.name == type
                val matchesSearch = search == null ||
                        store.storeName.contains(search, ignoreCase = true) ||
                        store.ownerName.contains(search, ignoreCase = true) ||
                        store.storeId.value.contains(search, ignoreCase = true)

                matchesStatus && matchesType && matchesSearch
            }

            // 페이징 적용
            val startIndex = page * size
            val pagedStores = if (startIndex < filteredStores.size) {
                filteredStores.drop(startIndex).take(size)
            } else {
                emptyList()
            }

            // DTO로 변환
            val storeDtos = pagedStores.map { store ->
                StoreDto(
                    storeId = store.storeId.value,
                    storeName = store.storeName,
                    storeType = store.storeType.name,
                    operationType = store.operationType?.name,
                    hqId = store.hqId?.value,
                    hqName = if (store.hqId != null) "${store.storeName} 본사" else null, // 임시
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
                    managerUsername = null, // TODO: 매장 관리자 정보 조회
                    posCount = 1, // TODO: 실제 POS 수 조회
                    employeeCount = 3, // TODO: 실제 직원 수 조회
                    isActive = store.isActive,
                    createdAt = store.createdAt,
                    updatedAt = store.updatedAt
                )
            }

            return ResponseEntity.ok(
                StoreListResponse(
                    stores = storeDtos,
                    totalCount = filteredStores.size.toLong(),
                    page = page,
                    size = size
                )
            )

        } catch (e: Exception) {
            // 오류 발생시 더미 데이터로 대체
            println("매장 목록 조회 중 오류 발생: ${e.message}")
            e.printStackTrace()

            val stores = listOf(
                StoreDto(
                    storeId = "HQ001001",
                    storeName = "강남점",
                    storeType = "CHAIN",
                    operationType = "DIRECT",
                    hqId = "HQHQ1",
                    hqName = "커피왕 본사",
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
                    managerUsername = "manager1",
                    posCount = 3,
                    employeeCount = 5,
                    isActive = true,
                    createdAt = java.time.LocalDateTime.now().minusYears(2),
                    updatedAt = java.time.LocalDateTime.now()
                ),
                StoreDto(
                    storeId = "IN002001",
                    storeName = "개인카페 A",
                    storeType = "INDIVIDUAL",
                    operationType = null,
                    hqId = null,
                    hqName = null,
                    regionCode = "002",
                    regionName = "부산광역시",
                    storeNumber = "001",
                    businessLicense = "234-56-78901",
                    ownerName = "이사장",
                    phoneNumber = "051-2345-6789",
                    address = "부산광역시 해운대구 해운대로 456",
                    postalCode = "48094",
                    openingDate = java.time.LocalDate.now().minusMonths(6),
                    storeStatus = "ACTIVE",
                    managerUsername = null,
                    posCount = 1,
                    employeeCount = 2,
                    isActive = true,
                    createdAt = java.time.LocalDateTime.now().minusMonths(6),
                    updatedAt = java.time.LocalDateTime.now()
                )
            )

            return ResponseEntity.ok(
                StoreListResponse(
                    stores = stores,
                    totalCount = stores.size.toLong(),
                    page = page,
                    size = size
                )
            )
        }
    }

    @GetMapping("/{storeId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getStore(@PathVariable storeId: String): ResponseEntity<StoreDto> {

        // TODO: 실제 매장 상세 조회 로직 구현
        val store = StoreDto(
            storeId = storeId,
            storeName = "강남점",
            storeType = "CHAIN",
            operationType = "DIRECT",
            hqId = "HQHQ1",
            hqName = "커피왕 본사",
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
            managerUsername = "manager1",
            posCount = 3,
            employeeCount = 5,
            isActive = true,
            createdAt = java.time.LocalDateTime.now().minusYears(2),
            updatedAt = java.time.LocalDateTime.now()
        )

        return ResponseEntity.ok(store)
    }

    @PostMapping
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun createStore(
        @Valid @RequestBody request: CreateStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StoreDto> {

        // TODO: 실제 매장 생성 로직 구현
        val newStore = StoreDto(
            storeId = generateStoreId(request.storeType, request.hqCode, request.regionCode, request.storeNumber),
            storeName = request.storeName,
            storeType = request.storeType,
            operationType = request.operationType,
            hqId = request.hqId,
            hqName = request.hqName,
            regionCode = request.regionCode,
            regionName = request.regionName ?: "",
            storeNumber = request.storeNumber,
            businessLicense = request.businessLicense,
            ownerName = request.ownerName,
            phoneNumber = request.phoneNumber,
            address = request.address,
            postalCode = request.postalCode,
            openingDate = request.openingDate ?: java.time.LocalDate.now(),
            storeStatus = "ACTIVE",
            managerUsername = null,
            posCount = 0,
            employeeCount = 0,
            isActive = true,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(newStore)
    }

    @PutMapping("/{storeId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun updateStore(
        @PathVariable storeId: String,
        @Valid @RequestBody request: UpdateStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StoreDto> {

        // TODO: 실제 매장 업데이트 로직 구현
        val updatedStore = StoreDto(
            storeId = storeId,
            storeName = request.storeName,
            storeType = "CHAIN",
            operationType = "DIRECT",
            hqId = "HQHQ1",
            hqName = "커피왕 본사",
            regionCode = "001",
            regionName = "서울특별시",
            storeNumber = "001",
            businessLicense = request.businessLicense,
            ownerName = request.ownerName,
            phoneNumber = request.phoneNumber,
            address = request.address,
            postalCode = request.postalCode,
            openingDate = java.time.LocalDate.now().minusYears(2),
            storeStatus = request.storeStatus ?: "ACTIVE",
            managerUsername = null,
            posCount = 3,
            employeeCount = 5,
            isActive = true,
            createdAt = java.time.LocalDateTime.now().minusYears(2),
            updatedAt = java.time.LocalDateTime.now()
        )

        return ResponseEntity.ok(updatedStore)
    }

    @DeleteMapping("/{storeId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.DELETE)
    suspend fun deleteStore(
        @PathVariable storeId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {

        // TODO: 실제 매장 삭제 로직 구현
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/regions")
    suspend fun getRegions(): ResponseEntity<List<RegionDto>> {

        // TODO: 실제 지역 목록 조회 로직 구현
        val regions = listOf(
            RegionDto("001", "서울특별시"),
            RegionDto("002", "부산광역시"),
            RegionDto("003", "대구광역시"),
            RegionDto("004", "인천광역시"),
            RegionDto("009", "경기도")
        )

        return ResponseEntity.ok(regions)
    }

    @GetMapping("/headquarters")
    suspend fun getHeadquarters(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<List<HeadquartersDto>> {

        try {
            // JWT 토큰에서 사용자 정보 추출
            val token = authorization.removePrefix("Bearer ")
            val username = jwtUtil.getUsernameFromToken(token)

            // 사용자 정보 조회
            val user = userRepository.findByUsername(username)
                ?: return ResponseEntity.ok(emptyList())

            val userRoles = user.roles.map { it.name }.toSet()

            // 권한에 따른 본사 목록 조회
            val headquarters = when {
                // 시스템 관리자나 슈퍼 관리자 - 모든 본사 조회 가능
                userRoles.contains("SYSTEM_ADMIN") || userRoles.contains("SUPER_ADMIN") -> {
                    try {
                        val allHq = headquartersRepository.findAll()
                        allHq.map { hq ->
                            HeadquartersDto(
                                hqId = hq.hqId.value,
                                hqCode = hq.hqCode,
                                hqName = hq.hqName
                            )
                        }
                    } catch (e: Exception) {
                        // DB 조회 실패시 더미 데이터
                        listOf(
                            HeadquartersDto("HQSTT", "STT", "스타트업 테스트 본사"),
                            HeadquartersDto("HQTET", "TET", "테스트 본사")
                        )
                    }
                }
                // 본사 관리자나 본사 매니저 - 자신의 본사만 조회 가능
                userRoles.contains("HEADQUARTERS_ADMIN") || userRoles.contains("HQ_MANAGER") -> {
                    if (user.organizationId != null && user.organizationType == "HEADQUARTERS") {
                        try {
                            val hq = headquartersRepository.findByHqId(HeadquartersId(user.organizationId))
                            if (hq != null) {
                                listOf(
                                    HeadquartersDto(
                                        hqId = hq.hqId.value,
                                        hqCode = hq.hqCode,
                                        hqName = hq.hqName
                                    )
                                )
                            } else {
                                emptyList()
                            }
                        } catch (e: Exception) {
                            listOf(HeadquartersDto("HQSTT", "STT", "내 본사"))
                        }
                    } else {
                        emptyList()
                    }
                }
                else -> {
                    emptyList()
                }
            }

            return ResponseEntity.ok(headquarters)

        } catch (e: Exception) {
            // 인증 오류나 기타 오류시 빈 목록 반환
            println("본사 목록 조회 중 오류 발생: ${e.message}")
            return ResponseEntity.ok(emptyList())
        }
    }

    private fun generateStoreId(storeType: String, hqCode: String?, regionCode: String, storeNumber: String): String {
        return if (storeType == "CHAIN" && hqCode != null) {
            "${hqCode}${regionCode}${storeNumber.padStart(3, '0')}"
        } else {
            "IN${regionCode}${storeNumber.padStart(3, '0')}"
        }
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

data class StoreListResponse(
    val stores: List<StoreDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class StoreDto(
    val storeId: String,
    val storeName: String,
    val storeType: String,
    val operationType: String?,
    val hqId: String?,
    val hqName: String?,
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
    val managerUsername: String?,
    val posCount: Int,
    val employeeCount: Int,
    val isActive: Boolean,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class CreateStoreRequest(
    @field:jakarta.validation.constraints.NotBlank
    val storeName: String,
    @field:jakarta.validation.constraints.NotBlank
    val storeType: String, // CHAIN, INDIVIDUAL
    val operationType: String?, // DIRECT, FRANCHISE (for CHAIN only)
    val hqId: String?,
    val hqCode: String?,
    val hqName: String?,
    @field:jakarta.validation.constraints.NotBlank
    val regionCode: String,
    val regionName: String?,
    @field:jakarta.validation.constraints.NotBlank
    val storeNumber: String,
    val businessLicense: String?,
    @field:jakarta.validation.constraints.NotBlank
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: java.time.LocalDate?
)

data class UpdateStoreRequest(
    val storeName: String,
    val businessLicense: String?,
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val storeStatus: String?
)

data class RegionDto(
    val regionCode: String,
    val regionName: String
)

data class HeadquartersDto(
    val hqId: String,
    val hqCode: String,
    val hqName: String
)

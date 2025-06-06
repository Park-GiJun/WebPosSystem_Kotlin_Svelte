package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.entities.PermissionType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/business/stores")
class BusinessStoreController {

    @GetMapping
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getStores(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<StoreListResponse> {
        
        // TODO: 실제 매장 목록 조회 로직 구현
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
        
        return ResponseEntity.ok(StoreListResponse(
            stores = stores,
            totalCount = stores.size.toLong(),
            page = page,
            size = size
        ))
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
    suspend fun getHeadquarters(): ResponseEntity<List<HeadquartersDto>> {
        
        // TODO: 실제 본사 목록 조회 로직 구현
        val headquarters = listOf(
            HeadquartersDto("HQHQ1", "HQ1", "커피왕 본사"),
            HeadquartersDto("HQHQ2", "HQ2", "베이커리킹 본사"),
            HeadquartersDto("HQHQ3", "HQ3", "프랜차이즈마스터 본사")
        )
        
        return ResponseEntity.ok(headquarters)
    }

    private fun generateStoreId(storeType: String, hqCode: String?, regionCode: String, storeNumber: String): String {
        return if (storeType == "CHAIN" && hqCode != null) {
            "${hqCode}${regionCode}${storeNumber.padStart(3, '0')}"
        } else {
            "IN${regionCode}${storeNumber.padStart(3, '0')}"
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

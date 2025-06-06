package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.entities.PermissionType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/business/pos")
class BusinessPosController {

    @GetMapping
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getPosSystems(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) storeId: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?
    ): ResponseEntity<PosListResponse> {
        
        // TODO: 실제 POS 시스템 목록 조회 로직 구현
        val posSystems = listOf(
            PosDto(
                posId = "HQ001001P01",
                storeId = "HQ001001",
                storeName = "강남점",
                posNumber = 1,
                posName = "메인 POS",
                posType = "MAIN",
                ipAddress = "192.168.1.101",
                macAddress = "00:1B:44:11:3A:B7",
                serialNumber = "POS001234567",
                installedDate = java.time.LocalDate.now().minusYears(1),
                lastMaintenanceDate = java.time.LocalDate.now().minusMonths(3),
                posStatus = "ACTIVE",
                isActive = true,
                createdAt = java.time.LocalDateTime.now().minusYears(1),
                updatedAt = java.time.LocalDateTime.now()
            ),
            PosDto(
                posId = "HQ001001P02",
                storeId = "HQ001001",
                storeName = "강남점",
                posNumber = 2,
                posName = "서브 POS",
                posType = "SUB",
                ipAddress = "192.168.1.102",
                macAddress = "00:1B:44:11:3A:B8",
                serialNumber = "POS001234568",
                installedDate = java.time.LocalDate.now().minusMonths(8),
                lastMaintenanceDate = java.time.LocalDate.now().minusMonths(1),
                posStatus = "ACTIVE",
                isActive = true,
                createdAt = java.time.LocalDateTime.now().minusMonths(8),
                updatedAt = java.time.LocalDateTime.now()
            ),
            PosDto(
                posId = "IN002001P01",
                storeId = "IN002001",
                storeName = "개인카페 A",
                posNumber = 1,
                posName = "메인 POS",
                posType = "MAIN",
                ipAddress = "192.168.1.201",
                macAddress = "00:1B:44:11:3A:C1",
                serialNumber = "POS001234569",
                installedDate = java.time.LocalDate.now().minusMonths(6),
                lastMaintenanceDate = java.time.LocalDate.now().minusWeeks(2),
                posStatus = "MAINTENANCE",
                isActive = true,
                createdAt = java.time.LocalDateTime.now().minusMonths(6),
                updatedAt = java.time.LocalDateTime.now()
            )
        )
        
        val filteredPos = posSystems.filter { pos ->
            (storeId == null || pos.storeId == storeId) &&
            (status == null || pos.posStatus == status) &&
            (type == null || pos.posType == type)
        }
        
        return ResponseEntity.ok(PosListResponse(
            posSystems = filteredPos,
            totalCount = filteredPos.size.toLong(),
            page = page,
            size = size
        ))
    }

    @GetMapping("/{posId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getPosSystem(@PathVariable posId: String): ResponseEntity<PosDto> {
        
        // TODO: 실제 POS 시스템 상세 조회 로직 구현
        val pos = PosDto(
            posId = posId,
            storeId = "HQ001001",
            storeName = "강남점",
            posNumber = 1,
            posName = "메인 POS",
            posType = "MAIN",
            ipAddress = "192.168.1.101",
            macAddress = "00:1B:44:11:3A:B7",
            serialNumber = "POS001234567",
            installedDate = java.time.LocalDate.now().minusYears(1),
            lastMaintenanceDate = java.time.LocalDate.now().minusMonths(3),
            posStatus = "ACTIVE",
            isActive = true,
            createdAt = java.time.LocalDateTime.now().minusYears(1),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.ok(pos)
    }

    @PostMapping
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun createPosSystem(
        @Valid @RequestBody request: CreatePosRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<PosDto> {
        
        // TODO: 실제 POS 시스템 생성 로직 구현
        val newPos = PosDto(
            posId = generatePosId(request.storeId, request.posNumber),
            storeId = request.storeId,
            storeName = request.storeName ?: "Unknown Store",
            posNumber = request.posNumber,
            posName = request.posName ?: "POS ${request.posNumber}",
            posType = request.posType,
            ipAddress = request.ipAddress,
            macAddress = request.macAddress,
            serialNumber = request.serialNumber,
            installedDate = request.installedDate ?: java.time.LocalDate.now(),
            lastMaintenanceDate = java.time.LocalDate.now(),
            posStatus = "ACTIVE",
            isActive = true,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(newPos)
    }

    @PutMapping("/{posId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun updatePosSystem(
        @PathVariable posId: String,
        @Valid @RequestBody request: UpdatePosRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<PosDto> {
        
        // TODO: 실제 POS 시스템 업데이트 로직 구현
        val updatedPos = PosDto(
            posId = posId,
            storeId = "HQ001001",
            storeName = "강남점",
            posNumber = 1,
            posName = request.posName ?: "POS 1",
            posType = request.posType ?: "MAIN",
            ipAddress = request.ipAddress,
            macAddress = request.macAddress,
            serialNumber = request.serialNumber,
            installedDate = java.time.LocalDate.now().minusYears(1),
            lastMaintenanceDate = java.time.LocalDate.now(),
            posStatus = request.posStatus ?: "ACTIVE",
            isActive = true,
            createdAt = java.time.LocalDateTime.now().minusYears(1),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.ok(updatedPos)
    }

    @DeleteMapping("/{posId}")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.DELETE)
    suspend fun deletePosSystem(
        @PathVariable posId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 POS 시스템 삭제 로직 구현
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{posId}/maintenance")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun performMaintenance(
        @PathVariable posId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 POS 시스템 점검 로직 구현
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{posId}/complete-maintenance")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun completeMaintenance(
        @PathVariable posId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 POS 시스템 점검 완료 로직 구현
        return ResponseEntity.ok().build()
    }

    private fun generatePosId(storeId: String, posNumber: Int): String {
        val paddedPosNumber = posNumber.toString().padStart(2, '0')
        return "${storeId}P${paddedPosNumber}"
    }
}

data class PosListResponse(
    val posSystems: List<PosDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class PosDto(
    val posId: String,
    val storeId: String,
    val storeName: String,
    val posNumber: Int,
    val posName: String?,
    val posType: String,
    val ipAddress: String?,
    val macAddress: String?,
    val serialNumber: String?,
    val installedDate: java.time.LocalDate,
    val lastMaintenanceDate: java.time.LocalDate,
    val posStatus: String,
    val isActive: Boolean,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class CreatePosRequest(
    @field:jakarta.validation.constraints.NotBlank
    val storeId: String,
    val storeName: String?,
    @field:jakarta.validation.constraints.Min(1)
    val posNumber: Int,
    val posName: String?,
    @field:jakarta.validation.constraints.NotBlank
    val posType: String, // MAIN, SUB, MOBILE
    val ipAddress: String?,
    val macAddress: String?,
    val serialNumber: String?,
    val installedDate: java.time.LocalDate?
)

data class UpdatePosRequest(
    val posName: String?,
    val posType: String?,
    val ipAddress: String?,
    val macAddress: String?,
    val serialNumber: String?,
    val posStatus: String?
)

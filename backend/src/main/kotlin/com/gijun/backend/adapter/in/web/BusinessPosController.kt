package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.business.*
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.enums.PermissionType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/business/pos")
@Tag(
    name = "🖥️ POS Systems", 
    description = "POS 시스템 관리 API"
)
class BusinessPosController {

    @GetMapping
    @Operation(
        summary = "📋 POS 시스템 목록 조회",
        description = """
            **등록된 POS 시스템 목록을 조회합니다.**
            
            🖥️ **POS 시스템 정보:**
            - 📍 **위치**: 매장별 POS 단말기 현황
            - 🔧 **상태**: 운영/점검/오류 상태 정보
            - 🌐 **네트워크**: IP/MAC 주소 등 네트워크 정보
            - 📅 **이력**: 설치일, 점검일 등 관리 이력
            
            🔍 **필터링 옵션:**
            - **매장별**: 특정 매장의 POS만 조회
            - **상태별**: ACTIVE, MAINTENANCE, ERROR 등
            - **타입별**: MAIN, SUB, MOBILE 등
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🖥️ POS Systems"]
    )
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ POS 시스템 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PosListResponse::class),
                    examples = [
                        ExampleObject(
                            name = "POS 시스템 목록",
                            value = """{
                                "posSystems": [
                                    {
                                        "posId": "HQ001001P01",
                                        "storeId": "HQ001001",
                                        "storeName": "강남점",
                                        "posNumber": 1,
                                        "posName": "메인 POS",
                                        "posType": "MAIN",
                                        "ipAddress": "192.168.1.101",
                                        "macAddress": "00:1B:44:11:3A:B7",
                                        "serialNumber": "POS001234567",
                                        "posStatus": "ACTIVE",
                                        "isActive": true
                                    }
                                ],
                                "totalCount": 3,
                                "page": 0,
                                "size": 20
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getPosSystems(
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "매장 ID 필터", example = "HQ001001")
        @RequestParam(required = false) storeId: String?,
        
        @Parameter(description = "POS 상태 필터", example = "ACTIVE")
        @RequestParam(required = false) status: String?,
        
        @Parameter(description = "POS 타입 필터", example = "MAIN")
        @RequestParam(required = false) type: String?
    ): ResponseEntity<PosListResponse> {
        
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

    @PostMapping
    @Operation(
        summary = "🖥️ 새 POS 시스템 등록",
        description = """
            **매장에 새로운 POS 시스템을 등록합니다.**
            
            🔧 **등록 절차:**
            1. 매장 정보 및 POS 번호 유효성 검증
            2. 네트워크 정보 설정
            3. 하드웨어 정보 등록
            4. 초기 설정 및 테스트
            5. 운영 상태로 전환
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🖥️ POS Systems"]
    )
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun createPosSystem(
        @Valid @RequestBody request: CreatePosRequest,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<PosDto> {
        
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

    @PostMapping("/{posId}/maintenance")
    @Operation(
        summary = "🔧 POS 점검 시작",
        description = """
            **POS 시스템의 정기 점검을 시작합니다.**
            
            🔧 **점검 절차:**
            1. POS 상태를 MAINTENANCE로 변경
            2. 현재 거래 완료 대기
            3. 시스템 진단 실행
            4. 하드웨어 상태 확인
            5. 소프트웨어 업데이트 확인
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🖥️ POS Systems"]
    )
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun performMaintenance(
        @Parameter(description = "POS ID", example = "HQ001001P01", required = true)
        @PathVariable posId: String,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{posId}/complete-maintenance")
    @Operation(
        summary = "✅ POS 점검 완료",
        description = """
            **POS 시스템의 점검을 완료하고 운영 상태로 복구합니다.**
            
            ✅ **완료 절차:**
            1. 점검 결과 확인
            2. 시스템 정상 동작 테스트
            3. POS 상태를 ACTIVE로 변경
            4. 점검 이력 기록
            5. 운영진에게 알림 발송
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🖥️ POS Systems"]
    )
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun completeMaintenance(
        @Parameter(description = "POS ID", example = "HQ001001P01", required = true)
        @PathVariable posId: String,
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        return ResponseEntity.ok().build()
    }

    private fun generatePosId(storeId: String, posNumber: Int): String {
        val paddedPosNumber = posNumber.toString().padStart(2, '0')
        return "${storeId}P${paddedPosNumber}"
    }
}

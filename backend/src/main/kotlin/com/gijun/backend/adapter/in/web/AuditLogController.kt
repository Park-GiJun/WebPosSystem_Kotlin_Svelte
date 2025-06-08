package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.AuditLogService
import com.gijun.backend.application.service.AuditLogDto
import com.gijun.backend.application.service.AuditLogListResponse
import com.gijun.backend.application.service.AuditStatisticsDto
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.enums.PermissionType
import com.gijun.backend.domain.audit.enums.AuditActionType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/admin/audit")
@Tag(
    name = "📋 Audit Logs", 
    description = "시스템 감사 로그 조회 API"
)
class AuditLogController(
    private val auditLogService: AuditLogService
) {

    @GetMapping
    @RequiresPermission(menuCode = "ADMIN_AUDIT", permission = PermissionType.READ)
    @Operation(
        summary = "📋 감사 로그 목록 조회",
        description = """
            **시스템에서 발생한 모든 감사 로그를 조회합니다.**
            
            🔍 **조회 기능:**
            - 페이징 처리된 감사 로그 목록
            - 다양한 필터링 옵션 (테이블, 액션, 사용자, 기간)
            - 검색 기능 (설명, 레코드 ID)
            - 날짜 범위 필터링
            
            📊 **조회 정보:**
            - 테이블명 및 레코드 ID
            - 액션 타입 (생성/수정/삭제)
            - 변경 전후 값
            - 실행 사용자 정보
            - IP 주소 및 사용자 에이전트
            - 실행 시간
            
            🔐 **권한:**
            - ADMIN_AUDIT 메뉴 READ 권한 필요
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["📋 Audit Logs"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 감사 로그 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AuditLogListResponse::class)
                )]
            )
        ]
    )
    suspend fun getAuditLogs(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "테이블명 필터", example = "users")
        @RequestParam(required = false) tableName: String?,
        
        @Parameter(description = "액션 타입 필터", example = "UPDATE")
        @RequestParam(required = false) actionType: String?,
        
        @Parameter(description = "사용자 ID 필터", example = "admin")
        @RequestParam(required = false) userId: String?,
        
        @Parameter(description = "사용자명 필터", example = "admin")
        @RequestParam(required = false) userName: String?,
        
        @Parameter(description = "검색어 (설명, 레코드 ID)", example = "매장")
        @RequestParam(required = false) searchTerm: String?,
        
        @Parameter(description = "시작 날짜", example = "2025-06-01T00:00:00")
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
        startDate: LocalDateTime?,
        
        @Parameter(description = "종료 날짜", example = "2025-06-30T23:59:59")
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
        endDate: LocalDateTime?,
        
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AuditLogListResponse> {
        
        val auditActionType = actionType?.let { 
            try { AuditActionType.fromString(it) } catch (e: Exception) { null }
        }
        
        val response = auditLogService.getAuditLogs(
            page = page,
            size = size,
            tableName = tableName,
            actionType = auditActionType,
            userId = userId,
            userName = userName,
            searchTerm = searchTerm,
            startDate = startDate,
            endDate = endDate
        )
        
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @RequiresPermission(menuCode = "ADMIN_AUDIT", permission = PermissionType.READ)
    @Operation(
        summary = "📄 감사 로그 상세 조회",
        description = """
            **특정 감사 로그의 상세 정보를 조회합니다.**
            
            📋 **제공 정보:**
            - 변경 전후 값의 상세 비교
            - JSON 형태의 변경 필드 정보
            - 사용자 세션 및 요청 정보
            - 상세 설명 및 메타데이터
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["📋 Audit Logs"]
    )
    suspend fun getAuditLogById(
        @Parameter(description = "감사 로그 ID", example = "1")
        @PathVariable id: Long,
        
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AuditLogDto> {
        
        val auditLog = auditLogService.getAuditLogById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(auditLog)
    }

    @GetMapping("/record/{tableName}/{recordId}")
    @RequiresPermission(menuCode = "ADMIN_AUDIT", permission = PermissionType.READ)
    @Operation(
        summary = "📚 특정 레코드 변경 이력 조회",
        description = """
            **특정 테이블의 특정 레코드에 대한 모든 변경 이력을 조회합니다.**
            
            🔍 **조회 범위:**
            - 레코드 생성부터 현재까지의 모든 변경사항
            - 시간순 정렬된 변경 이력
            - 각 변경사항의 상세 정보
            
            📊 **활용 사례:**
            - 사용자 계정 변경 추적
            - 매장 정보 수정 이력
            - 권한 변경 기록 확인
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["📋 Audit Logs"]
    )
    suspend fun getRecordHistory(
        @Parameter(description = "테이블명", example = "users")
        @PathVariable tableName: String,
        
        @Parameter(description = "레코드 ID", example = "admin")
        @PathVariable recordId: String,
        
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<List<AuditLogDto>> {
        
        val history = auditLogService.getRecordHistory(tableName, recordId)
        return ResponseEntity.ok(history)
    }

    @GetMapping("/statistics")
    @RequiresPermission(menuCode = "ADMIN_AUDIT", permission = PermissionType.READ)
    @Operation(
        summary = "📊 감사 로그 통계 조회",
        description = """
            **시스템 감사 로그의 통계 정보를 조회합니다.**
            
            📈 **통계 정보:**
            - 액션 타입별 발생 건수 (생성/수정/삭제)
            - 테이블별 변경 빈도
            - 사용자별 활동 통계
            - 지정 기간 내 총 이벤트 수
            
            🎯 **활용:**
            - 시스템 사용 패턴 분석
            - 사용자 활동 모니터링
            - 보안 이상 징후 탐지
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["📋 Audit Logs"]
    )
    suspend fun getStatistics(
        @Parameter(description = "통계 조회 기간 (일)", example = "7")
        @RequestParam(defaultValue = "7") days: Int,
        
        @Parameter(description = "JWT 토큰", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AuditStatisticsDto> {
        
        val statistics = auditLogService.getStatistics(days)
        return ResponseEntity.ok(statistics)
    }
}

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.business.DashboardStatsResponse
import com.gijun.backend.configuration.PermissionChecker
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(
    name = "📊 Dashboard", 
    description = "대시보드 통계 및 현황 API"
)
class DashboardController(
    private val permissionChecker: PermissionChecker
) {

    @GetMapping("/stats")
    @Operation(
        summary = "📈 대시보드 통계 조회",
        description = """
            **메인 대시보드에 표시할 핵심 통계 정보를 조회합니다.**
            
            📊 **통계 정보:**
            - 🏢 **총 매장 수**: 등록된 전체 매장 개수
            - 👥 **총 사용자 수**: 시스템 등록 사용자 수
            - 💰 **총 매출액**: 당일/당월 누적 매출
            - 🖥️ **활성 POS**: 현재 동작 중인 POS 단말기 수
            
            🔐 **권한**: 인증된 사용자만 접근 가능
            ⚡ **실시간**: 최신 데이터 반영 (5분 간격 업데이트)
            
            💡 **활용 사례:**
            - 관리자 메인 화면 대시보드
            - 실시간 현황 모니터링
            - 운영 현황 요약 보고서
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["📊 Dashboard"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 대시보드 통계 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = DashboardStatsResponse::class),
                    examples = [
                        ExampleObject(
                            name = "정상 통계 데이터",
                            summary = "시스템이 정상 운영 중인 경우의 통계",
                            value = """{
                                "totalStores": 125,
                                "totalUsers": 348,
                                "totalSales": 15750000,
                                "activePos": 89,
                                "todaySales": 2450000,
                                "monthSales": 15750000,
                                "newUsersToday": 3,
                                "onlineStores": 118
                            }"""
                        ),
                        ExampleObject(
                            name = "신규 시스템",
                            summary = "새로 설치된 시스템의 초기 상태",
                            value = """{
                                "totalStores": 1,
                                "totalUsers": 2,
                                "totalSales": 0,
                                "activePos": 1,
                                "todaySales": 0,
                                "monthSales": 0,
                                "newUsersToday": 2,
                                "onlineStores": 1
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "토큰 없음",
                            value = """{
                                "success": false,
                                "message": "인증 토큰이 필요합니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{
                                "success": false,
                                "message": "토큰이 만료되었습니다. 다시 로그인해주세요",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "🚫 권한 없음",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "대시보드 조회 권한이 없습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "500",
                description = "❌ 서버 오류",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "통계 데이터 조회 중 오류가 발생했습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getDashboardStats(
        @Parameter(
            description = "JWT 인증 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<DashboardStatsResponse> {
        // 인증된 사용자만 접근 가능
        permissionChecker.extractUserIdFromToken(authorization)
        
        // TODO: 실제 통계 데이터 조회 로직 구현
        // 현재는 임시 데이터 반환
        
        val stats = DashboardStatsResponse(
            totalStores = 125,
            totalUsers = 348,
            totalSales = 15_750_000,
            activePos = 89,
            todaySales = 2_450_000,
            monthSales = 15_750_000,
            newUsersToday = 3,
            onlineStores = 118
        )
        
        return ResponseEntity.ok(stats)
    }
}

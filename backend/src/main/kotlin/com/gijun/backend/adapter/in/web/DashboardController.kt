package com.gijun.backend.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController {

    @GetMapping("/stats")
    suspend fun getDashboardStats(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<DashboardStatsResponse> {
        // TODO: 실제 통계 데이터 조회 로직 구현
        // 현재는 임시 데이터 반환
        
        val stats = DashboardStatsResponse(
            totalStores = 125,
            totalUsers = 348,
            totalSales = 15_750_000,
            activePos = 89
        )
        
        return ResponseEntity.ok(stats)
    }
}

data class DashboardStatsResponse(
    val totalStores: Int,
    val totalUsers: Int,
    val totalSales: Long,
    val activePos: Int
)
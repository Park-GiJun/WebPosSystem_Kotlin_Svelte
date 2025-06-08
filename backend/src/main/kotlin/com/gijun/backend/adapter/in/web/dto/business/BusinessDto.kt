package com.gijun.backend.adapter.`in`.web.dto.business

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "POS 시스템 목록 응답")
data class PosListResponse(
    @Schema(description = "POS 시스템 목록")
    val posSystems: List<PosDto>,
    
    @Schema(description = "전체 POS 수", example = "25")
    val totalCount: Long,
    
    @Schema(description = "현재 페이지", example = "0")
    val page: Int,
    
    @Schema(description = "페이지 크기", example = "20")
    val size: Int
)

@Schema(description = "POS 시스템 정보")
data class PosDto(
    @Schema(description = "POS ID", example = "HQ001001P01")
    val posId: String,
    
    @Schema(description = "매장 ID", example = "HQ001001")
    val storeId: String,
    
    @Schema(description = "매장명", example = "강남점")
    val storeName: String,
    
    @Schema(description = "POS 번호", example = "1")
    val posNumber: Int,
    
    @Schema(description = "POS 이름", example = "메인 POS")
    val posName: String?,
    
    @Schema(description = "POS 타입", example = "MAIN")
    val posType: String,
    
    @Schema(description = "IP 주소", example = "192.168.1.101")
    val ipAddress: String?,
    
    @Schema(description = "MAC 주소", example = "00:1B:44:11:3A:B7")
    val macAddress: String?,
    
    @Schema(description = "시리얼 번호", example = "POS001234567")
    val serialNumber: String?,
    
    @Schema(description = "설치일")
    val installedDate: LocalDate,
    
    @Schema(description = "최종 점검일")
    val lastMaintenanceDate: LocalDate,
    
    @Schema(description = "POS 상태", example = "ACTIVE")
    val posStatus: String,
    
    @Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,
    
    @Schema(description = "수정 시간")
    val updatedAt: LocalDateTime
)

@Schema(description = "POS 시스템 생성 요청")
data class CreatePosRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "매장 ID는 필수입니다")
    @Schema(description = "매장 ID", example = "HQ001001", required = true)
    val storeId: String,
    
    @Schema(description = "매장명", example = "강남점")
    val storeName: String?,
    
    @field:jakarta.validation.constraints.Min(value = 1, message = "POS 번호는 1 이상이어야 합니다")
    @Schema(description = "POS 번호", example = "1", required = true)
    val posNumber: Int,
    
    @Schema(description = "POS 이름", example = "메인 POS")
    val posName: String?,
    
    @field:jakarta.validation.constraints.NotBlank(message = "POS 타입은 필수입니다")
    @Schema(description = "POS 타입 (MAIN, SUB, MOBILE, KIOSK)", example = "MAIN", required = true)
    val posType: String,
    
    @Schema(description = "IP 주소", example = "192.168.1.101")
    val ipAddress: String?,
    
    @Schema(description = "MAC 주소", example = "00:1B:44:11:3A:B7")
    val macAddress: String?,
    
    @Schema(description = "시리얼 번호", example = "POS001234567")
    val serialNumber: String?,
    
    @Schema(description = "설치일")
    val installedDate: LocalDate?
)

@Schema(description = "대시보드 통계 응답 데이터")
data class DashboardStatsResponse(
    @Schema(description = "전체 매장 수", example = "125")
    val totalStores: Int,
    
    @Schema(description = "전체 사용자 수", example = "348")
    val totalUsers: Int,
    
    @Schema(description = "총 누적 매출액 (원)", example = "15750000")
    val totalSales: Long,
    
    @Schema(description = "현재 활성화된 POS 수", example = "89")
    val activePos: Int,
    
    @Schema(description = "오늘 매출액 (원)", example = "2450000")
    val todaySales: Long? = null,
    
    @Schema(description = "이번 달 매출액 (원)", example = "15750000")
    val monthSales: Long? = null,
    
    @Schema(description = "오늘 신규 사용자 수", example = "3")
    val newUsersToday: Int? = null,
    
    @Schema(description = "온라인 상태 매장 수", example = "118")
    val onlineStores: Int? = null
)

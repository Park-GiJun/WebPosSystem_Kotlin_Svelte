package com.gijun.backend.adapter.`in`.web.dto.business

import java.time.LocalDate
import java.time.LocalDateTime

// Store DTOs
data class BusinessStoreDto(
    val storeId: String,
    val storeName: String,
    val storeType: String,
    val operationType: String?,
    val hqId: String?,
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: String?,
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: LocalDate?,
    val storeStatus: String,
    val managerUserId: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateBusinessStoreRequest(
    @field:jakarta.validation.constraints.NotBlank
    val storeName: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val operationType: String, // DIRECT, FRANCHISE
    
    @field:jakarta.validation.constraints.NotBlank
    val regionCode: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val storeNumber: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val ownerName: String,
    
    val businessLicense: String?,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: LocalDate?,
    
    // Manager Account Info
    @field:jakarta.validation.constraints.NotBlank
    val managerUsername: String,
    
    @field:jakarta.validation.constraints.Email
    val managerEmail: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val managerPassword: String
)

data class UpdateBusinessStoreRequest(
    val storeName: String?,
    val ownerName: String?,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val storeStatus: String?
)

data class BusinessStoreListResponse(
    val stores: List<BusinessStoreDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

// POS DTOs
data class BusinessPosDto(
    val posId: String,
    val storeId: String,
    val storeName: String?,
    val posName: String,
    val posType: String,
    val posNumber: Int,
    val serialNumber: String?,
    val ipAddress: String?,
    val macAddress: String?,
    val location: String?,
    val description: String?,
    val posStatus: String,
    val lastActiveAt: LocalDateTime?,
    val installedDate: LocalDate?,
    val lastMaintenanceDate: LocalDate?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateBusinessPosRequest(
    @field:jakarta.validation.constraints.NotBlank
    val storeId: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val posName: String,
    
    @field:jakarta.validation.constraints.NotBlank
    val posType: String, // MAIN, SUB, MOBILE
    
    val posNumber: Int = 1,
    val serialNumber: String?,
    val ipAddress: String?,
    val macAddress: String?,
    val location: String?,
    val description: String?
)

data class UpdateBusinessPosRequest(
    val posName: String?,
    val location: String?,
    val description: String?,
    val posStatus: String?
)

data class BusinessPosListResponse(
    val posDevices: List<BusinessPosDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

// Dashboard DTOs
data class DashboardStatsResponse(
    val totalStores: Long,
    val activeStores: Long,
    val totalPosDevices: Long,
    val onlinePosDevices: Long,
    val todaySales: SalesStatsDto?,
    val recentActivities: List<ActivityDto>
)

data class SalesStatsDto(
    val totalAmount: Double,
    val totalTransactions: Long,
    val averageTicket: Double,
    val comparedToYesterday: Double
)

data class ActivityDto(
    val id: String,
    val type: String, // STORE_CREATED, POS_ADDED, etc.
    val description: String,
    val entityId: String,
    val entityName: String,
    val performedBy: String,
    val occurredAt: LocalDateTime
)

// Common Response DTOs
data class BusinessOperationResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

data class BusinessErrorResponse(
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

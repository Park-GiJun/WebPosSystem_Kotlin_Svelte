package com.gijun.backend.adapter.`in`.web.dto.organization

import java.time.LocalDate
import java.time.LocalDateTime

// Request DTOs
data class CreateHeadquartersRequest(
    val hqCode: String,
    val hqName: String,
    val businessLicense: String?,
    val ceoName: String?,
    val address: String?,
    val contactPhone: String?,
    val website: String?,
    val adminUsername: String,
    val adminEmail: String,
    val adminPassword: String
)

data class CreateIndividualStoreRequest(
    val storeName: String,
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: String?,
    val ownerName: String,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: LocalDate?,
    val adminUsername: String,
    val adminEmail: String,
    val adminPassword: String
)

// Response DTOs
data class HeadquartersResponse(
    val hqId: String,
    val hqCode: String,
    val hqName: String,
    val businessLicense: String?,
    val ceoName: String?,
    val address: String?,
    val contactPhone: String?,
    val website: String?,
    val adminUser: UserResponse,
    val createdAt: LocalDateTime
)

data class StoreResponse(
    val storeId: String,
    val storeName: String,
    val storeType: String,
    val operationType: String?,
    val regionCode: String,
    val storeNumber: String,
    val ownerName: String,
    val businessLicense: String?,
    val phoneNumber: String?,
    val address: String?,
    val postalCode: String?,
    val openingDate: LocalDate?,
    val storeStatus: String,
    val managerUser: UserResponse,
    val createdAt: LocalDateTime
)

data class OrganizationsResponse(
    val headquarters: List<HeadquartersResponse>,
    val individualStores: List<StoreResponse>
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String
)

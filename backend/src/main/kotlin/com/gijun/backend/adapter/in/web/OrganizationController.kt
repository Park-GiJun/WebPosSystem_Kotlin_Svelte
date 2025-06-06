package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.*
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.entities.PermissionType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/organizations")
class SuperAdminOrganizationController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/headquarters")
    @RequiresPermission(menuCode = "ADMIN_ORGANIZATIONS", permission = PermissionType.WRITE)
    suspend fun createHeadquarters(
        @Valid @RequestBody request: CreateHeadquartersRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<HeadquartersResponse> {
        val token = authorization.removePrefix("Bearer ")
        val result = organizationService.createHeadquarters(request, token)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PostMapping("/individual-stores")
    @RequiresPermission(menuCode = "ADMIN_ORGANIZATIONS", permission = PermissionType.WRITE)
    suspend fun createIndividualStore(
        @Valid @RequestBody request: CreateIndividualStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StoreResponse> {
        val token = authorization.removePrefix("Bearer ")
        val result = organizationService.createIndividualStore(request, token)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/headquarters")
    @RequiresPermission(menuCode = "ADMIN_ORGANIZATIONS", permission = PermissionType.READ)
    suspend fun getHeadquarters(): ResponseEntity<OrganizationListResponse> {
        val headquarters = organizationService.getAllHeadquarters()
        return ResponseEntity.ok(OrganizationListResponse(
            headquarters = headquarters,
            individualStores = emptyList()
        ))
    }

    @GetMapping("/individual-stores")
    @RequiresPermission(menuCode = "ADMIN_ORGANIZATIONS", permission = PermissionType.READ)
    suspend fun getIndividualStores(): ResponseEntity<OrganizationListResponse> {
        val stores = organizationService.getAllIndividualStores()
        return ResponseEntity.ok(OrganizationListResponse(
            headquarters = emptyList(),
            individualStores = stores
        ))
    }

    @GetMapping
    @RequiresPermission(menuCode = "ADMIN_ORGANIZATIONS", permission = PermissionType.READ)
    suspend fun getAllOrganizations(): ResponseEntity<OrganizationListResponse> {
        val headquarters = organizationService.getAllHeadquarters()
        val individualStores = organizationService.getAllIndividualStores()
        return ResponseEntity.ok(OrganizationListResponse(
            headquarters = headquarters,
            individualStores = individualStores
        ))
    }
}

@RestController
@RequestMapping("/api/v1/business/headquarters")
class HeadquartersController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/stores")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.WRITE)
    suspend fun createChainStore(
        @Valid @RequestBody request: CreateChainStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StoreResponse> {
        val token = authorization.removePrefix("Bearer ")
        val result = organizationService.createChainStore(request, token)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/my-stores")
    @RequiresPermission(menuCode = "BUSINESS_STORES", permission = PermissionType.READ)
    suspend fun getMyStores(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<HeadquartersStoreListResponse> {
        val token = authorization.removePrefix("Bearer ")
        val stores = organizationService.getStoresByHeadquarters(token)
        return ResponseEntity.ok(HeadquartersStoreListResponse(
            stores = stores,
            totalCount = stores.size.toLong(),
            page = 0,
            size = stores.size
        ))
    }
}

@RestController
@RequestMapping("/api/v1/stores")
class StoreController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/users")
    @RequiresPermission(menuCode = "POS_SETTINGS", permission = PermissionType.WRITE)
    suspend fun createStoreUser(
        @Valid @RequestBody request: CreateStoreUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserResponse> {
        val token = authorization.removePrefix("Bearer ")
        val result = organizationService.createStoreUser(request, token)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PostMapping("/pos-systems")
    @RequiresPermission(menuCode = "POS_SETTINGS", permission = PermissionType.WRITE)
    suspend fun createPosSystem(
        @Valid @RequestBody request: CreatePosSystemRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<PosSystemResponse> {
        val token = authorization.removePrefix("Bearer ")
        val result = organizationService.createPosSystem(request, token)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }
}

data class OrganizationListResponse(
    val headquarters: List<HeadquartersResponse>,
    val individualStores: List<StoreResponse>
)

data class HeadquartersStoreListResponse(
    val stores: List<StoreResponse>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.organization.*
import com.gijun.backend.application.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/admin/organizations")
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/headquarters")
    suspend fun createHeadquarters(
        @Valid @RequestBody request: CreateHeadquartersRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<HeadquartersResponse> {
        return try {
            val token = authorization.removePrefix("Bearer ")
            val response = organizationService.createHeadquarters(request, token)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "본사 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @PostMapping("/individual-stores")
    suspend fun createIndividualStore(
        @Valid @RequestBody request: CreateIndividualStoreRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<StoreResponse> {
        return try {
            val token = authorization.removePrefix("Bearer ")
            val response = organizationService.createIndividualStore(request, token)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "개인매장 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @GetMapping
    suspend fun getOrganizations(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<OrganizationsResponse> {
        return try {
            // TODO: 조직 목록 조회 구현
            val response = OrganizationsResponse(
                headquarters = emptyList(),
                individualStores = emptyList()
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "조직 목록 조회 중 오류가 발생했습니다: ${e.message}")
        }
    }
}

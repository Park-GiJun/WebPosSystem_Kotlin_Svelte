package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.organization.*
import com.gijun.backend.application.service.*
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
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/admin/organizations")
@Tag(
    name = "🏢 Organizations", 
    description = "조직(본사/매장) 관리 API"
)
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/headquarters")
    @Operation(
        summary = "🏢 본사 생성",
        description = """
            **새로운 본사를 생성하고 관리자 계정을 자동으로 설정합니다.**
            
            🏗️ **생성 절차:**
            1. 본사 정보 유효성 검증
            2. 중복 사업자번호 확인
            3. 본사 조직 생성
            4. 기본 관리자 계정 자동 생성
            5. 초기 권한 설정
            
            👨‍💼 **자동 생성되는 관리자:**
            - 사용자명: 입력한 관리자명
            - 이메일: 입력한 이메일
            - 권한: HEADQUARTERS_ADMIN
            - 상태: 활성화
            
            ⚠️ **주의사항:**
            - 사업자번호는 10자리 숫자만 입력
            - 이메일은 관리자 계정 생성에 사용됨
            - 생성 후 이메일로 초기 비밀번호 전송
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🏢 Organizations"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "✅ 본사 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = HeadquartersResponse::class),
                    examples = [
                        ExampleObject(
                            name = "본사 생성 완료",
                            summary = "본사와 관리자 계정이 성공적으로 생성된 경우",
                            value = """{
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "name": "ABC 유통",
                                "businessNumber": "1234567890",
                                "address": "서울시 강남구 테헤란로 123",
                                "phoneNumber": "02-1234-5678",
                                "email": "admin@abcdist.com",
                                "adminUser": {
                                    "id": "987fcdeb-51d2-4321-ba98-765432109876",
                                    "username": "abc_admin",
                                    "email": "admin@abcdist.com",
                                    "roles": ["HEADQUARTERS_ADMIN"],
                                    "userStatus": "ACTIVE"
                                },
                                "createdAt": "2025-06-07T21:00:00",
                                "updatedAt": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "❌ 잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "중복 사업자번호",
                            value = """{
                                "success": false,
                                "message": "이미 등록된 사업자번호입니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        ),
                        ExampleObject(
                            name = "유효성 검증 실패",
                            value = """{
                                "success": false,
                                "message": "사업자번호는 10자리 숫자여야 합니다",
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
                                "message": "본사 생성 권한이 없습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun createHeadquarters(
        @Valid @RequestBody request: CreateHeadquartersRequest,
        @Parameter(
            description = "관리자 JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
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
    @Operation(
        summary = "🏪 개인매장 생성",
        description = """
            **새로운 개인 매장을 생성하고 매장 관리자 계정을 자동으로 설정합니다.**
            
            🏗️ **생성 절차:**
            1. 매장 정보 유효성 검증
            2. 중복 사업자번호 확인 (개인매장용)
            3. 매장 조직 생성
            4. 매장 관리자 계정 자동 생성
            5. 초기 POS 설정 및 권한 부여
            
            👨‍💼 **자동 생성되는 매장 관리자:**
            - 사용자명: 입력한 관리자명
            - 이메일: 입력한 이메일
            - 권한: STORE_ADMIN
            - 상태: 활성화
            
            💰 **POS 시스템:**
            - 기본 POS 단말기 1대 자동 등록
            - 기본 상품 카테고리 생성
            - 결제 방식 설정
            
            ⚠️ **개인매장 특징:**
            - 본사에 소속되지 않는 독립 매장
            - 단일 관리자 체계
            - 간소화된 권한 구조
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🏢 Organizations"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "✅ 개인매장 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StoreResponse::class),
                    examples = [
                        ExampleObject(
                            name = "개인매장 생성 완료",
                            value = """{
                                "id": "456e7890-e12b-34d5-c678-901234567890",
                                "name": "행복편의점",
                                "businessNumber": "9876543210",
                                "address": "부산시 해운대구 센텀로 456",
                                "phoneNumber": "051-987-6543",
                                "email": "owner@happystore.com",
                                "storeType": "INDIVIDUAL",
                                "adminUser": {
                                    "id": "654e3210-f98a-7654-b321-098765432109",
                                    "username": "happy_owner",
                                    "email": "owner@happystore.com",
                                    "roles": ["STORE_ADMIN"],
                                    "userStatus": "ACTIVE"
                                },
                                "posCount": 1,
                                "isActive": true,
                                "createdAt": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "❌ 잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "중복 사업자번호",
                            value = """{
                                "success": false,
                                "message": "이미 등록된 매장 사업자번호입니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun createIndividualStore(
        @Valid @RequestBody request: CreateIndividualStoreRequest,
        @Parameter(
            description = "관리자 JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
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
    @Operation(
        summary = "📋 조직 목록 조회",
        description = """
            **시스템에 등록된 모든 조직(본사/매장) 목록을 조회합니다.**
            
            📊 **조회 정보:**
            - 🏢 **본사 목록**: 등록된 모든 본사 정보
            - 🏪 **개인매장 목록**: 독립 운영되는 개인매장 정보
            - 📈 **기본 통계**: 각 조직별 매장 수, 사용자 수 등
            
            🔐 **권한별 조회 범위:**
            - **시스템 관리자**: 모든 조직 정보
            - **본사 관리자**: 자신의 본사 및 산하 매장
            - **매장 관리자**: 자신의 매장 정보만
            
            🔍 **필터링 옵션:**
            - 조직 타입별 필터링 가능
            - 상태별 필터링 (활성/비활성)
            - 지역별 검색 지원
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🏢 Organizations"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 조직 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationsResponse::class),
                    examples = [
                        ExampleObject(
                            name = "전체 조직 목록",
                            summary = "시스템 관리자가 조회하는 전체 조직 목록",
                            value = """{
                                "headquarters": [
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "name": "ABC 유통",
                                        "businessNumber": "1234567890",
                                        "storeCount": 15,
                                        "userCount": 48,
                                        "isActive": true
                                    }
                                ],
                                "individualStores": [
                                    {
                                        "id": "456e7890-e12b-34d5-c678-901234567890",
                                        "name": "행복편의점",
                                        "businessNumber": "9876543210",
                                        "posCount": 1,
                                        "isActive": true
                                    }
                                ],
                                "totalCount": 16,
                                "summary": {
                                    "totalHeadquarters": 1,
                                    "totalStores": 15,
                                    "totalIndividualStores": 1,
                                    "totalUsers": 49
                                }
                            }"""
                        ),
                        ExampleObject(
                            name = "빈 목록",
                            summary = "조직이 없는 경우",
                            value = """{
                                "headquarters": [],
                                "individualStores": [],
                                "totalCount": 0,
                                "summary": {
                                    "totalHeadquarters": 0,
                                    "totalStores": 0,
                                    "totalIndividualStores": 0,
                                    "totalUsers": 1
                                }
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
                                "message": "조직 목록 조회 권한이 없습니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getOrganizations(
        @Parameter(
            description = "JWT 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<OrganizationsResponse> {
        return try {
            val token = authorization.removePrefix("Bearer ")
            val response = organizationService.getOrganizations(token)
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "조직 목록 조회 중 오류가 발생했습니다: ${e.message}")
        }
    }
}

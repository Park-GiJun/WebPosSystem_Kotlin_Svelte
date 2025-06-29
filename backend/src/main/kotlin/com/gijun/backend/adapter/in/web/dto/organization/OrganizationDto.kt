package com.gijun.backend.adapter.`in`.web.dto.organization

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "본사 생성 요청")
data class CreateHeadquartersRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "본사명은 필수입니다")
    @Schema(description = "본사명", example = "ABC 유통", required = true)
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "사업자번호는 필수입니다")
    @field:jakarta.validation.constraints.Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 10자리 숫자여야 합니다")
    @Schema(description = "사업자번호", example = "1234567890", required = true)
    val businessNumber: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "주소는 필수입니다")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", required = true)
    val address: String,
    
    @Schema(description = "전화번호", example = "02-1234-5678")
    val phoneNumber: String?,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "관리자 이메일", example = "admin@abcdist.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "관리자명은 필수입니다")
    @Schema(description = "관리자명", example = "abc_admin", required = true)
    val adminUsername: String
)

@Schema(description = "개인매장 생성 요청")
data class CreateIndividualStoreRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "매장명은 필수입니다")
    @Schema(description = "매장명", example = "행복편의점", required = true)
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "사업자번호는 필수입니다")
    @field:jakarta.validation.constraints.Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 10자리 숫자여야 합니다")
    @Schema(description = "사업자번호", example = "9876543210", required = true)
    val businessNumber: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "주소는 필수입니다")
    @Schema(description = "주소", example = "부산시 해운대구 센텀로 456", required = true)
    val address: String,
    
    @Schema(description = "전화번호", example = "051-987-6543")
    val phoneNumber: String?,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "사장 이메일", example = "owner@happystore.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "사장명은 필수입니다")
    @Schema(description = "사장명", example = "happy_owner", required = true)
    val ownerUsername: String
)

@Schema(description = "본사 응답")
data class HeadquartersResponse(
    @Schema(description = "본사 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,
    
    @Schema(description = "본사명", example = "ABC 유통")
    val name: String,
    
    @Schema(description = "사업자번호", example = "1234567890")
    val businessNumber: String,
    
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String,
    
    @Schema(description = "전화번호", example = "02-1234-5678")
    val phoneNumber: String?,
    
    @Schema(description = "이메일", example = "admin@abcdist.com")
    val email: String,
    
    @Schema(description = "관리자 정보")
    val adminUser: AdminUserInfo,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,
    
    @Schema(description = "수정 시간")
    val updatedAt: LocalDateTime
)

@Schema(description = "매장 응답")
data class StoreResponse(
    @Schema(description = "매장 ID", example = "456e7890-e12b-34d5-c678-901234567890")
    val id: String,
    
    @Schema(description = "매장명", example = "행복편의점")
    val name: String,
    
    @Schema(description = "사업자번호", example = "9876543210")
    val businessNumber: String,
    
    @Schema(description = "주소", example = "부산시 해운대구 센텀로 456")
    val address: String,
    
    @Schema(description = "전화번호", example = "051-987-6543")
    val phoneNumber: String?,
    
    @Schema(description = "이메일", example = "owner@happystore.com")
    val email: String,
    
    @Schema(description = "매장 타입", example = "INDIVIDUAL")
    val storeType: String,
    
    @Schema(description = "관리자 정보")
    val adminUser: AdminUserInfo,
    
    @Schema(description = "POS 단말기 수", example = "1")
    val posCount: Int,
    
    @Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime
)

@Schema(description = "관리자 정보")
data class AdminUserInfo(
    @Schema(description = "사용자 ID", example = "987fcdeb-51d2-4321-ba98-765432109876")
    val id: String,
    
    @Schema(description = "사용자명", example = "abc_admin")
    val username: String,
    
    @Schema(description = "이메일", example = "admin@abcdist.com")
    val email: String,
    
    @Schema(description = "역할 목록", example = "[\"HEADQUARTERS_ADMIN\"]")
    val roles: List<String>,
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val userStatus: String
)

@Schema(description = "조직 목록 응답")
data class OrganizationsResponse(
    @Schema(description = "본사 목록")
    val headquarters: List<HeadquartersSummaryDto>,
    
    @Schema(description = "개인매장 목록")
    val individualStores: List<StoreSummaryDto>,
    
    @Schema(description = "전체 조직 수", example = "16")
    val totalCount: Int,
    
    @Schema(description = "조직 통계 요약")
    val summary: OrganizationSummary
)

@Schema(description = "조직 통계 요약")
data class OrganizationSummary(
    @Schema(description = "총 본사 수", example = "1")
    val totalHeadquarters: Int,
    
    @Schema(description = "총 매장 수", example = "15")
    val totalStores: Int,
    
    @Schema(description = "총 개인매장 수", example = "1")
    val totalIndividualStores: Int,
    
    @Schema(description = "총 사용자 수", example = "49")
    val totalUsers: Int
)

@Schema(description = "본사 요약 정보")
data class HeadquartersSummaryDto(
    @Schema(description = "본사 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,
    
    @Schema(description = "본사명", example = "ABC 유통")
    val name: String,
    
    @Schema(description = "사업자번호", example = "1234567890")
    val businessNumber: String,
    
    @Schema(description = "산하 매장 수", example = "15")
    val storeCount: Int,
    
    @Schema(description = "소속 사용자 수", example = "48")
    val userCount: Int,
    
    @Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean
)

@Schema(description = "본사의 체인점 생성 요청")
data class CreateChainStoreRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "매장명은 필수입니다")
    @Schema(description = "매장명", example = "ABC마트 강남점", required = true)
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "지역코드는 필수입니다")
    @Schema(description = "지역코드", example = "001", required = true)
    val regionCode: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "매장번호는 필수입니다")
    @Schema(description = "매장번호", example = "001", required = true)
    val storeNumber: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "주소는 필수입니다")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 456", required = true)
    val address: String,
    
    @Schema(description = "전화번호", example = "02-555-1234")
    val phoneNumber: String?,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "매장 관리자 이메일", example = "gangnam@abcmart.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "매장 관리자명은 필수입니다")
    @Schema(description = "매장 관리자명", example = "gangnam_manager", required = true)
    val managerUsername: String,
    
    @Schema(description = "매장 사업자번호 (본사와 다른 경우)")
    val businessNumber: String?
)

@Schema(description = "매장 요약 정보")
data class StoreSummaryDto(
    @Schema(description = "매장 ID", example = "456e7890-e12b-34d5-c678-901234567890")
    val id: String,
    
    @Schema(description = "매장명", example = "행복편의점")
    val name: String,
    
    @Schema(description = "사업자번호", example = "9876543210")
    val businessNumber: String,
    
    @Schema(description = "POS 단말기 수", example = "1")
    val posCount: Int,
    
    @Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean
)

@Schema(description = "본사의 체인점 생성 요청")
data class CreateChainStoreRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "매장명은 필수입니다")
    @Schema(description = "매장명", example = "ABC마트 강남점", required = true)
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "지역코드는 필수입니다")
    @Schema(description = "지역코드", example = "001", required = true)
    val regionCode: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "매장번호는 필수입니다")
    @Schema(description = "매장번호", example = "001", required = true)
    val storeNumber: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "주소는 필수입니다")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 456", required = true)
    val address: String,
    
    @Schema(description = "전화번호", example = "02-555-1234")
    val phoneNumber: String?,
    
    @field:jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "매장 관리자 이메일", example = "gangnam@abcmart.com", required = true)
    val email: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "매장 관리자명은 필수입니다")
    @Schema(description = "매장 관리자명", example = "gangnam_manager", required = true)
    val managerUsername: String,
    
    @Schema(description = "매장 사업자번호 (본사와 다른 경우)")
    val businessNumber: String?
)

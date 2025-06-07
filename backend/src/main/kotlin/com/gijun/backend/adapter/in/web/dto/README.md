# DTO 패키지 구조

## 📦 패키지 설명

### `auth/`
- 인증 관련 DTO들
- `LoginRequest`, `LoginResponse`, `RegisterRequest` 등

### `user/`
- 사용자 관리 관련 DTO들
- `AdminUserDto`, `CreateUserRequest`, `UpdateUserRequest` 등

### `organization/`
- 조직(본사/매장) 관리 관련 DTO들
- `CreateHeadquartersRequest`, `StoreResponse` 등

### `permission/`
- 권한 및 메뉴 관리 관련 DTO들
- `MenuDto`, `PermissionDto`, `GrantPermissionRequest` 등

### `business/`
- 영업정보시스템 관련 DTO들
- `BusinessStoreDto`, `BusinessPosDto`, `DashboardStatsResponse` 등

### `common/`
- 공통으로 사용되는 DTO들
- `ApiResponse`, `PagedResponse`, `ErrorResponse` 등

## 🎯 명명 규칙

- **Request DTOs**: `*Request` 접미사 사용
- **Response DTOs**: `*Response` 접미사 사용
- **Entity DTOs**: `*Dto` 접미사 사용
- **List/Paged DTOs**: `*ListResponse` 또는 `*PagedResponse` 사용

## 📋 Validation

모든 Request DTO는 Bean Validation 어노테이션을 사용하여 검증 규칙을 정의합니다:

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50)
    val username: String,
    
    @field:Email(message = "Invalid email format")
    val email: String
)
```

## 🔄 매핑 규칙

- Controller → Service: Request DTO 사용
- Service → Controller: Response DTO 또는 Entity DTO 사용
- Domain Entity ↔ DTO: 별도 Mapper 클래스에서 변환 처리

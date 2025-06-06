package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.*
import com.gijun.backend.application.service.AuthService
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.permission.entities.PermissionType
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {

    @GetMapping
    @RequiresPermission(menuCode = "ADMIN_USERS", permission = PermissionType.READ)
    suspend fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<UserListResponse> {
        
        // TODO: 실제로는 페이징과 필터링을 구현해야 함
        // 임시로 모든 사용자 반환
        val users = listOf(
            AdminUserDto(
                id = "1",
                username = "admin",
                email = "admin@webpos.com",
                roles = listOf("SUPER_ADMIN"),
                userStatus = "ACTIVE",
                isEmailVerified = true,
                lastLoginAt = null,
                failedLoginAttempts = 0,
                isLocked = false,
                lockedUntil = null,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            ),
            AdminUserDto(
                id = "2",
                username = "manager1",
                email = "manager1@webpos.com",
                roles = listOf("HQ_MANAGER"),
                userStatus = "ACTIVE",
                isEmailVerified = true,
                lastLoginAt = java.time.LocalDateTime.now().minusHours(2),
                failedLoginAttempts = 0,
                isLocked = false,
                lockedUntil = null,
                createdAt = java.time.LocalDateTime.now().minusDays(30),
                updatedAt = java.time.LocalDateTime.now().minusHours(2)
            )
        )
        
        return ResponseEntity.ok(UserListResponse(
            users = users,
            totalCount = users.size.toLong(),
            page = page,
            size = size
        ))
    }

    @PostMapping
    @RequiresPermission(menuCode = "ADMIN_USERS_CREATE", permission = PermissionType.WRITE)
    suspend fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // TODO: 실제 사용자 생성 로직 구현
        val newUser = AdminUserDto(
            id = java.util.UUID.randomUUID().toString(),
            username = request.username,
            email = request.email,
            roles = request.roles,
            userStatus = "PENDING_VERIFICATION",
            isEmailVerified = false,
            lastLoginAt = null,
            failedLoginAttempts = 0,
            isLocked = false,
            lockedUntil = null,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser)
    }

    @PutMapping("/{userId}")
    @RequiresPermission(menuCode = "ADMIN_USERS", permission = PermissionType.WRITE)
    suspend fun updateUser(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // TODO: 실제 사용자 업데이트 로직 구현
        val updatedUser = AdminUserDto(
            id = userId,
            username = request.username,
            email = request.email,
            roles = request.roles,
            userStatus = request.userStatus ?: "ACTIVE",
            isEmailVerified = true,
            lastLoginAt = null,
            failedLoginAttempts = 0,
            isLocked = false,
            lockedUntil = null,
            createdAt = java.time.LocalDateTime.now().minusDays(30),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{userId}")
    @RequiresPermission(menuCode = "ADMIN_USERS", permission = PermissionType.DELETE)
    suspend fun deleteUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 사용자 삭제 로직 구현
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{userId}/roles")
    @RequiresPermission(menuCode = "ADMIN_USERS_ROLES", permission = PermissionType.WRITE)
    suspend fun assignRoles(
        @PathVariable userId: String,
        @Valid @RequestBody request: AssignRoleRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<AdminUserDto> {
        
        // TODO: 실제 역할 할당 로직 구현
        val updatedUser = AdminUserDto(
            id = userId,
            username = "updated_user",
            email = "user@webpos.com",
            roles = request.roles,
            userStatus = "ACTIVE",
            isEmailVerified = true,
            lastLoginAt = null,
            failedLoginAttempts = 0,
            isLocked = false,
            lockedUntil = null,
            createdAt = java.time.LocalDateTime.now().minusDays(30),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return ResponseEntity.ok(updatedUser)
    }

    @PostMapping("/{userId}/unlock")
    @RequiresPermission(menuCode = "ADMIN_USERS", permission = PermissionType.WRITE)
    suspend fun unlockUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        
        // TODO: 실제 사용자 잠금 해제 로직 구현
        return ResponseEntity.ok().build()
    }
}

data class UserListResponse(
    val users: List<AdminUserDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class AdminUserDto(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String,
    val isEmailVerified: Boolean,
    val lastLoginAt: java.time.LocalDateTime?,
    val failedLoginAttempts: Int,
    val isLocked: Boolean,
    val lockedUntil: java.time.LocalDateTime?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class CreateUserRequest(
    @field:jakarta.validation.constraints.NotBlank
    val username: String,
    @field:jakarta.validation.constraints.Email
    val email: String,
    @field:jakarta.validation.constraints.NotBlank
    val password: String,
    val roles: List<String> = listOf("USER")
)

data class UpdateUserRequest(
    val username: String,
    val email: String,
    val roles: List<String>,
    val userStatus: String?
)

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.*
import com.gijun.backend.application.port.`in`.AuthUseCase
import com.gijun.backend.application.port.`in`.LoginCommand
import com.gijun.backend.application.port.`in`.RegisterCommand
import com.gijun.backend.application.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authUseCase: AuthUseCase,
    private val authService: AuthService
) {

    @PostMapping("/register")
    suspend fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        val command = RegisterCommand(
            username = request.username,
            email = request.email,
            password = request.password
        )

        val user = authUseCase.register(command)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AuthResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                roles = user.roles.map { it.name },
                userStatus = user.userStatus.name,
                isEmailVerified = user.isEmailVerified(),
                lastLoginAt = user.lastLoginAt,
                token = null // 등록 시에는 토큰 없음
            ))
    }

    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val command = LoginCommand(
            username = request.username,
            password = request.password
        )

        val authResult = authUseCase.login(command)

        return ResponseEntity.ok(AuthResponse(
            id = authResult.user.id,
            username = authResult.user.username,
            email = authResult.user.email,
            roles = authResult.user.roles.map { it.name },
            userStatus = authResult.user.userStatus.name,
            isEmailVerified = authResult.user.isEmailVerified(),
            lastLoginAt = authResult.user.lastLoginAt,
            token = authResult.token,
            expiresIn = authResult.expiresIn
        ))
    }

    @GetMapping("/me")
    suspend fun getCurrentUser(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserProfileResponse> {
        val token = authorization.removePrefix("Bearer ")
        val user = authUseCase.validateToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(UserProfileResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            roles = user.roles.map { it.name },
            userStatus = user.userStatus.name,
            isEmailVerified = user.isEmailVerified(),
            lastLoginAt = user.lastLoginAt,
            failedLoginAttempts = user.failedLoginAttempts,
            isLocked = user.isLocked(),
            lockedUntil = user.lockedUntil,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        ))
    }

    @PostMapping("/change-password")
    suspend fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val user = authUseCase.validateToken(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        authService.changePassword(
            userId = user.id,
            currentPassword = request.currentPassword,
            newPassword = request.newPassword,
            updatedBy = user.username
        )

        return ResponseEntity.ok("Password changed successfully")
    }

    @PostMapping("/verify-email/{userId}")
    suspend fun verifyEmail(
        @PathVariable userId: String
    ): ResponseEntity<String> {
        authService.verifyEmail(userId)
        return ResponseEntity.ok("Email verified successfully")
    }

    @PostMapping("/unlock/{userId}")
    suspend fun unlockUser(
        @PathVariable userId: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val currentUser = authUseCase.validateToken(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!currentUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        authService.unlockUser(userId, currentUser.username)
        return ResponseEntity.ok("User unlocked successfully")
    }
}
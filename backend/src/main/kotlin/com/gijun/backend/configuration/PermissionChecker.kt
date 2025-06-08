package com.gijun.backend.configuration

import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.domain.permission.enums.PermissionType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class PermissionChecker(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val permissionService: PermissionService
) {

    suspend fun checkPermission(
        authorizationHeader: String?,
        menuCode: String,
        requiredPermission: PermissionType = PermissionType.READ
    ): String {
        // Authorization 헤더 검증
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }

        val token = authorizationHeader.substring(7)
        
        // JWT 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }

        val username = jwtUtil.getUsernameFromToken(token)
        
        // 사용자 존재 확인
        val user = userRepository.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        // 사용자 상태 확인
        if (!user.canLogin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not accessible")
        }

        // 권한 체크
        val hasPermission = permissionService.checkUserPermission(
            username, 
            menuCode, 
            requiredPermission
        )

        if (!hasPermission) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Insufficient permissions for menu: $menuCode"
            )
        }

        return user.id
    }

    suspend fun extractUserIdFromToken(authorizationHeader: String?): String {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }

        val token = authorizationHeader.substring(7)
        
        if (!jwtUtil.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }

        val username = jwtUtil.getUsernameFromToken(token)
        val user = userRepository.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        return user.id
    }
}

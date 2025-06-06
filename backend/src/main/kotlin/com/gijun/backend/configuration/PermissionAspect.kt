package com.gijun.backend.configuration

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.common.util.logger
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class PermissionAspect(
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {
    private val logger = logger()

    @Around("@annotation(requiresPermission)")
    suspend fun checkPermission(joinPoint: ProceedingJoinPoint, requiresPermission: RequiresPermission): Any? {
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val request = requestAttributes?.request
            
            if (request == null) {
                logger.warn("No HTTP request found in context")
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
            }

            val authHeader = request.getHeader("Authorization")
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header")
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
            }

            val token = authHeader.substring(7)
            if (!jwtUtil.validateToken(token)) {
                logger.warn("Invalid JWT token")
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
            }

            val username = jwtUtil.getUsernameFromToken(token)
            
            // 권한 체크 (실제로는 username을 userId로 변환해야 함)
            val hasPermission = permissionService.checkUserPermission(
                username, 
                requiresPermission.menuCode, 
                requiresPermission.permission
            )

            if (!hasPermission) {
                logger.warn("User $username does not have permission ${requiresPermission.permission} for menu ${requiresPermission.menuCode}")
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions")
            }

            logger.debug("Permission check passed for user $username, menu ${requiresPermission.menuCode}, permission ${requiresPermission.permission}")
            
            return joinPoint.proceed()
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error during permission check", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Permission check failed")
        }
    }
}
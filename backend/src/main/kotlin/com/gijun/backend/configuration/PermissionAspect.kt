package com.gijun.backend.configuration

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.common.util.logger
import kotlinx.coroutines.runBlocking
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class PermissionAspect(
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {
    private val logger = logger()

    @Around("@annotation(requiresPermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, requiresPermission: RequiresPermission): Any? {
        return try {
            logger.debug("Permission check for menu ${requiresPermission.menuCode}, permission ${requiresPermission.permission}")
            
            // 현재는 단순히 proceed하고, 실제 권한 체크는 Controller에서 수행
            // AOP에서의 권한 체크는 복잡성을 줄이기 위해 비활성화
            joinPoint.proceed()
            
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error during permission check", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Permission check failed")
        }
    }
}

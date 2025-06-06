package com.gijun.backend.configuration

import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.common.util.logger
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Aspect
@Component
class PermissionAspect(
    private val permissionService: PermissionService,
    private val jwtUtil: JwtUtil
) {
    private val logger = logger()

    @Around("@annotation(requiresPermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, requiresPermission: RequiresPermission): Any? {
        return mono {
            try {
                // WebFlux 환경에서는 직접적인 request 접근이 어려우므로
                // Controller에서 Authorization 헤더를 직접 전달받도록 설계를 변경해야 합니다.
                
                logger.debug("Permission check for menu ${requiresPermission.menuCode}, permission ${requiresPermission.permission}")
                
                // 실제 권한 체크는 Controller에서 @RequestHeader로 받은 토큰으로 수행
                return@mono joinPoint.proceed()
                
            } catch (e: ResponseStatusException) {
                throw e
            } catch (e: Exception) {
                logger.error("Error during permission check", e)
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Permission check failed")
            }
        }.block()
    }
}

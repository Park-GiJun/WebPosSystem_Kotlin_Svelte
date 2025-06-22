package com.gijun.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * CORS 설정
 * WebFlux 환경에서 프론트엔드 도메인으로부터의 요청을 허용
 */
@Configuration
class CorsConfig {

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration().apply {
            // 허용할 Origin 설정
            setAllowedOriginPatterns(listOf(
                "https://gijun.net",
                "https://*.gijun.net",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://localhost:*"
            ))

            // 허용할 HTTP 메서드
            allowedMethods = listOf(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            )

            // 허용할 헤더
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Cache-Control"
            )

            // 자격 증명 허용 (쿠키, Authorization 헤더 등)
            allowCredentials = true

            // Preflight 요청 캐시 시간 (초)
            maxAge = 3600L

            // 응답 헤더 노출
            exposedHeaders = listOf(
                "Authorization",
                "Content-Disposition"
            )
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }

        return CorsWebFilter(source)
    }
}
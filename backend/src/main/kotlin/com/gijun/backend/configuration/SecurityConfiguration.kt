package com.gijun.backend.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationManager: JwtAuthenticationManager,
    private val jwtAuthenticationConverter: JwtAuthenticationConverter,
    private val environment: Environment
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager).apply {
            setServerAuthenticationConverter(jwtAuthenticationConverter)
        }

        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { exchanges ->
                exchanges
                    // OPTIONS 요청 모두 허용 (CORS preflight)
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Public endpoints - 인증 불필요
                    .pathMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/health", "/actuator/**").permitAll()
                    .pathMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                    .pathMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                    .pathMatchers("/ws/**").permitAll() // WebSocket endpoints
                    // Protected endpoints - JWT 인증 필요
                    .pathMatchers("/api/v1/auth/me").authenticated()
                    .pathMatchers("/api/v1/**").authenticated()
                    .anyExchange().permitAll()
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // 프로파일별 allowed origins 설정
            val activeProfiles = environment.activeProfiles
            val isProduction = activeProfiles.contains("prod")
            
            if (isProduction) {
                // 운영 환경 - 운영 도메인만 허용
                allowedOrigins = listOf(
                    "https://gijun.net",
                    "https://www.gijun.net",
                    "http://gijun.net",  // 리다이렉트용
                    "http://www.gijun.net"  // 리다이렉트용
                )
                println("Production CORS origins: ${allowedOrigins}")
            } else {
                // 개발 환경 - 로컬호스트 및 개발 도메인 허용
                allowedOriginPatterns = listOf("*")
                allowedOrigins = listOf(
                    "http://localhost:3000", 
                    "http://localhost:5173", 
                    "http://localhost:8080", 
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:5173",
                    "http://127.0.0.1:8080",
                    "https://gijun.net",
                    "https://www.gijun.net"
                )
                println("Development CORS origins: ${allowedOrigins}")
                println("Development CORS origin patterns: ${allowedOriginPatterns}")
            }
            
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
            allowCredentials = true
            maxAge = 3600L
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

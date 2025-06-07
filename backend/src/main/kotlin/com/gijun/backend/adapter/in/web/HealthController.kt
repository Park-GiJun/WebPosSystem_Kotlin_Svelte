package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.common.util.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/health")
@Tag(
    name = "⚙️ System", 
    description = "시스템 상태 확인 및 헬스체크 API"
)
class HealthController(
    private val databaseClient: DatabaseClient,
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
) {

    @GetMapping
    @Operation(
        summary = "🩺 전체 시스템 헬스체크",
        description = """
            **전체 시스템의 상태를 확인합니다.**
            
            ✅ **확인 항목:**
            - 📊 애플리케이션 상태
            - 🗄️ 데이터베이스 연결 상태  
            - 🔴 Redis 연결 상태
            - 💾 캐시 동작 상태
            
            💡 **상태 코드:**
            - `UP`: 정상 동작
            - `DOWN`: 서비스 중단
            - `PARTIAL`: 일부 서비스 장애
        """,
        tags = ["⚙️ System"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 헬스체크 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class),
                    examples = [
                        ExampleObject(
                            name = "정상 상태",
                            summary = "모든 서비스가 정상적으로 동작하는 경우",
                            value = """{
                                "success": true,
                                "data": {
                                    "status": "UP",
                                    "timestamp": "2024-06-07T21:00:00",
                                    "services": {
                                        "api": "UP",
                                        "database": "UP",
                                        "redis": "UP",
                                        "cache": "UP"
                                    }
                                },
                                "message": "🟢 시스템이 정상적으로 동작중입니다"
                            }"""
                        ),
                        ExampleObject(
                            name = "일부 서비스 장애",
                            summary = "일부 서비스에 문제가 있는 경우",
                            value = """{
                                "success": true,
                                "data": {
                                    "status": "PARTIAL",
                                    "timestamp": "2024-06-07T21:00:00",
                                    "services": {
                                        "api": "UP",
                                        "database": "DOWN",
                                        "redis": "UP",
                                        "cache": "DOWN"
                                    }
                                },
                                "message": "🟡 일부 서비스에 문제가 발생했습니다"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "503",
                description = "🚨 서비스 사용 불가",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class),
                    examples = [
                        ExampleObject(
                            name = "전체 서비스 장애",
                            value = """{
                                "success": false,
                                "data": null,
                                "message": "🔴 시스템이 일시적으로 사용할 수 없습니다"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun health(): ApiResponse<HealthCheckResponse> {
        return try {
            val services = mapOf(
                "api" to "UP",
                "database" to checkDatabase(),
                "redis" to checkRedis(),
                "cache" to checkCache()
            )
            
            val downServices = services.values.count { it == "DOWN" }
            val status = when {
                downServices == 0 -> "UP"
                downServices == services.size -> "DOWN"
                else -> "PARTIAL"
            }
            
            val response = HealthCheckResponse(
                status = status,
                timestamp = LocalDateTime.now(),
                services = services
            )
            
            val message = when (status) {
                "UP" -> "🟢 시스템이 정상적으로 동작중입니다"
                "PARTIAL" -> "🟡 일부 서비스에 문제가 발생했습니다"
                else -> "🔴 시스템에 문제가 발생했습니다"
            }
            
            ApiResponse.success(response, message)
        } catch (e: Exception) {
            ApiResponse.error("헬스체크 중 오류가 발생했습니다: ${e.message}")
        }
    }

    @GetMapping("/simple")
    @Operation(
        summary = "🔍 간단한 생존 신호 확인",
        description = """
            **애플리케이션의 기본적인 생존 여부만 빠르게 확인합니다.**
            
            🚀 **특징:**
            - 매우 빠른 응답 (외부 의존성 확인 없음)
            - 로드밸런서 헬스체크용으로 적합
            - 최소한의 리소스 사용
        """,
        tags = ["⚙️ System"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 애플리케이션 정상",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": true,
                                "data": {
                                    "status": "UP",
                                    "timestamp": "2024-06-07T21:00:00",
                                    "uptime": "운영 중"
                                },
                                "message": "🟢 애플리케이션이 정상적으로 동작중입니다"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun simpleHealth(): ApiResponse<SimpleHealthResponse> {
        val response = SimpleHealthResponse(
            status = "UP",
            timestamp = LocalDateTime.now(),
            uptime = "운영 중"
        )
        
        return ApiResponse.success(
            response, 
            "🟢 애플리케이션이 정상적으로 동작중입니다"
        )
    }

    @GetMapping("/component/{component}")
    @Operation(
        summary = "🔧 특정 컴포넌트 상태 확인",
        description = """
            **특정 시스템 컴포넌트의 상태를 개별적으로 확인합니다.**
            
            📋 **사용 가능한 컴포넌트:**
            - `database` - 데이터베이스 연결 상태
            - `redis` - Redis 연결 상태  
            - `cache` - 캐시 동작 상태
            - `api` - API 서버 상태
        """,
        tags = ["⚙️ System"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 컴포넌트 상태 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "데이터베이스 정상",
                            value = """{
                                "success": true,
                                "data": {
                                    "component": "database",
                                    "status": "UP",
                                    "details": {
                                        "connection": "active",
                                        "response_time": "5ms"
                                    }
                                },
                                "message": "🟢 database가 UP 상태입니다"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "❌ 존재하지 않는 컴포넌트",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "data": null,
                                "message": "❌ 요청한 컴포넌트를 찾을 수 없습니다"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun componentHealth(
        @Parameter(
            description = "확인할 컴포넌트 이름",
            example = "database",
            required = true
        )
        @PathVariable component: String
    ): ApiResponse<ComponentHealthResponse> {
        return try {
            val status = when (component) {
                "api" -> "UP"
                "database" -> checkDatabase()
                "redis" -> checkRedis()
                "cache" -> checkCache()
                else -> return ApiResponse.error("❌ 요청한 컴포넌트 '$component'를 찾을 수 없습니다")
            }
            
            val details = when (component) {
                "database" -> mapOf("connection" to if (status == "UP") "active" else "inactive")
                "redis" -> mapOf("connection" to if (status == "UP") "active" else "inactive")
                "cache" -> mapOf("operation" to if (status == "UP") "working" else "failed")
                else -> mapOf("service" to "api")
            }
            
            val response = ComponentHealthResponse(
                component = component,
                status = status,
                details = details
            )
            
            val statusEmoji = when (status) {
                "UP" -> "🟢"
                "DOWN" -> "🔴"
                else -> "⚪"
            }
            
            ApiResponse.success(
                response, 
                "$statusEmoji ${component}가 ${status} 상태입니다"
            )
        } catch (e: Exception) {
            ApiResponse.error("컴포넌트 상태 확인 중 오류가 발생했습니다: ${e.message}")
        }
    }

    private suspend fun checkDatabase(): String {
        return try {
            databaseClient.sql("SELECT 1")
                .fetch()
                .first()
                .awaitFirst()
            "UP"
        } catch (e: Exception) {
            "DOWN"
        }
    }

    private suspend fun checkRedis(): String {
        return try {
            redisTemplate.connectionFactory.reactiveConnection
                .ping()
                .awaitFirst()
            "UP"
        } catch (e: Exception) {
            "DOWN"
        }
    }

    private suspend fun checkCache(): String {
        return try {
            // 캐시에 테스트 데이터 저장/조회해서 정상 작동 확인
            val testKey = "health-check-${System.currentTimeMillis()}"
            
            redisTemplate.opsForValue()
                .set(testKey, "test", Duration.ofSeconds(5))
                .awaitFirstOrNull()

            val result = redisTemplate.opsForValue()
                .get(testKey)
                .awaitFirstOrNull()

            redisTemplate.delete(testKey).awaitFirstOrNull()

            if (result == "test") "UP" else "DOWN"
        } catch (e: Exception) {
            "DOWN"
        }
    }
}

@Schema(description = "전체 시스템 헬스체크 응답")
data class HealthCheckResponse(
    @Schema(description = "전체 시스템 상태", example = "UP", allowableValues = ["UP", "DOWN", "PARTIAL"])
    val status: String,
    
    @Schema(description = "응답 시간", example = "2024-06-07T21:00:00")
    val timestamp: LocalDateTime,
    
    @Schema(description = "각 서비스별 상태")
    val services: Map<String, String>
)

@Schema(description = "간단한 헬스체크 응답")
data class SimpleHealthResponse(
    @Schema(description = "애플리케이션 상태", example = "UP")
    val status: String,
    
    @Schema(description = "응답 시간", example = "2024-06-07T21:00:00")
    val timestamp: LocalDateTime,
    
    @Schema(description = "애플리케이션 가동 시간", example = "운영 중")
    val uptime: String
)

@Schema(description = "특정 컴포넌트 헬스체크 응답")
data class ComponentHealthResponse(
    @Schema(description = "컴포넌트 이름", example = "database")
    val component: String,
    
    @Schema(description = "컴포넌트 상태", example = "UP")
    val status: String,
    
    @Schema(description = "컴포넌트 상세 정보")
    val details: Map<String, Any>
)

// Custom Health Indicator for Actuator
@org.springframework.stereotype.Component
class DatabaseHealthIndicator(
    private val databaseClient: DatabaseClient
) : ReactiveHealthIndicator {

    override fun health(): Mono<Health> {
        return databaseClient.sql("SELECT 1")
            .fetch()
            .first()
            .map { Health.up().withDetail("database", "MySQL").build() }
            .onErrorResume { ex ->
                Mono.just(Health.down(ex).withDetail("database", "MySQL").build())
            }
    }
}

@org.springframework.stereotype.Component
class RedisHealthIndicator(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) : ReactiveHealthIndicator {

    override fun health(): Mono<Health> {
        return redisTemplate.connectionFactory.reactiveConnection
            .ping()
            .map { Health.up().withDetail("redis", "Available").build() }
            .onErrorResume { ex ->
                Mono.just(Health.down(ex).withDetail("redis", "Unavailable").build())
            }
    }
}

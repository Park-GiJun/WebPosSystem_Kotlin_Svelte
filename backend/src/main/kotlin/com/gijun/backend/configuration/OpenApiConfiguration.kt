package com.gijun.backend.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(
    @Value("\${api.title:WebPos API}") private val apiTitle: String,
    @Value("\${api.description:Hexagonal Architecture based WebPos Server API}") private val apiDescription: String,
    @Value("\${api.version:1.0.0}") private val apiVersion: String,
    @Value("\${api.contact.name:WebPos Team}") private val contactName: String,
    @Value("\${api.contact.email:support@webpos.com}") private val contactEmail: String,
    @Value("\${api.contact.url:https://github.com/yourusername/webpos}") private val contactUrl: String,
    @Value("\${api.license.name:MIT License}") private val licenseName: String,
    @Value("\${api.license.url:https://opensource.org/licenses/MIT}") private val licenseUrl: String,
    @Value("\${server.port:8080}") private val serverPort: String
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(apiInfo())
            .servers(servers())
            .externalDocs(externalDocumentation())
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("🔐 JWT Authorization header using the Bearer scheme")
                    )
            )
            .tags(predefinedTags())
    }

    @Bean
    fun authApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("🔐 인증 & 권한")
            .pathsToMatch("/api/v1/auth/**")
            .build()
    }

    @Bean
    fun userApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("👥 사용자 관리")
            .pathsToMatch("/api/v1/users/**")
            .build()
    }

    @Bean
    fun organizationApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("🏢 조직 관리")
            .pathsToMatch("/api/v1/headquarters/**", "/api/v1/stores/**")
            .build()
    }

    @Bean
    fun menuApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("📋 메뉴 & 권한")
            .pathsToMatch("/api/v1/menus/**", "/api/v1/permissions/**")
            .build()
    }

    @Bean
    fun productApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("🛍️ 상품 관리")
            .pathsToMatch("/api/v1/products/**", "/api/v1/categories/**")
            .build()
    }

    @Bean
    fun posApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("💰 POS 시스템")
            .pathsToMatch("/api/v1/pos/**", "/api/v1/sales/**", "/api/v1/orders/**")
            .build()
    }

    @Bean
    fun systemApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("⚙️ 시스템")
            .pathsToMatch("/api/v1/health/**", "/actuator/**")
            .build()
    }

    @Bean
    fun websocketApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("🔌 WebSocket")
            .pathsToMatch("/ws/**")
            .build()
    }

    private fun apiInfo(): Info {
        return Info()
            .title("🏪 $apiTitle")
            .description(buildDescription())
            .version("v$apiVersion")
            .termsOfService("https://webpos.com/terms")
            .contact(
                Contact()
                    .name("👥 $contactName")
                    .email("📧 $contactEmail")
                    .url("🌐 $contactUrl")
            )
            .license(
                License()
                    .name("📄 $licenseName")
                    .url(licenseUrl)
            )
    }

    private fun buildDescription(): String {
        return """
            🚀 **Hexagonal Architecture 기반의 현대적인 WebPos 시스템**
            
            ## 📋 주요 기능
            
            ### 🔐 인증 & 보안
            - JWT 기반 인증 시스템
            - 역할 기반 접근 제어 (RBAC)
            - 다중 조직 지원
            
            ### 🏢 조직 관리
            - 본사/매장 계층 구조
            - 자동 관리자 계정 생성
            - 조직별 권한 관리
            
            ### 💰 POS 시스템
            - 실시간 주문 처리
            - 재고 관리
            - 판매 통계
            
            ### ⚡ 기술 스택
            - **Backend**: Kotlin + Spring Boot 3 + WebFlux
            - **Database**: MySQL 8.0 + R2DBC
            - **Cache**: Redis
            - **Message Queue**: Apache Kafka
            - **Architecture**: Hexagonal (Clean Architecture)
            
            ## 🔗 관련 링크
            - [📖 API 문서](http://localhost:$serverPort/swagger-ui.html)
            - [🩺 헬스체크](http://localhost:$serverPort/actuator/health)
            - [📊 메트릭](http://localhost:$serverPort/actuator/prometheus)
            
            ---
            💡 **Tip**: 각 API 그룹별로 탭을 나누어 보기 쉽게 구성되어 있습니다.
        """.trimIndent()
    }

    private fun servers(): List<Server> {
        return listOf(
            Server().url("http://localhost:$serverPort").description("🏠 로컬 개발 서버"),
            Server().url("https://dev-api.webpos.com").description("🚧 개발 서버"),
            Server().url("https://staging-api.webpos.com").description("🎭 스테이징 서버"),
            Server().url("https://api.webpos.com").description("🌟 프로덕션 서버")
        )
    }

    private fun externalDocumentation(): ExternalDocumentation {
        return ExternalDocumentation()
            .description("📚 WebPos 시스템 전체 문서")
            .url("https://docs.webpos.com")
    }

    private fun predefinedTags(): List<Tag> {
        return listOf(
            Tag().name("🔐 Authentication").description("로그인, 토큰 관리 등 인증 관련 API"),
            Tag().name("👥 Users").description("사용자 계정 관리 API"),
            Tag().name("🏢 Organizations").description("본사/매장 조직 관리 API"),
            Tag().name("📋 Menus").description("메뉴 및 권한 관리 API"),
            Tag().name("🛍️ Products").description("상품 및 카테고리 관리 API"),
            Tag().name("💰 POS").description("POS 시스템 관련 API"),
            Tag().name("📊 Sales").description("매출 및 주문 관리 API"),
            Tag().name("⚙️ System").description("시스템 관리 및 모니터링 API"),
            Tag().name("🔌 WebSocket").description("실시간 통신 API")
        )
    }
}

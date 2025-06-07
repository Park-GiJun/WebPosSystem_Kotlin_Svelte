package com.gijun.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.time.Duration

@Configuration
@EnableWebFlux
class WebConfiguration : WebFluxConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Swagger UI 커스텀 CSS 파일 서빙
        registry.addResourceHandler("/swagger-ui-custom.css")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))

        // Swagger UI 커스텀 JavaScript 파일 서빙
        registry.addResourceHandler("/swagger-ui-custom.js")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))

        // 기타 정적 리소스 서빙
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))

        // Swagger UI 리소스
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))
    }

    @Bean
    fun staticResourceRouter(): RouterFunction<ServerResponse> = router {
        // 커스텀 CSS 파일 라우팅
        GET("/swagger-ui-custom.css") {
            val resource = ClassPathResource("static/swagger-ui-custom.css")
            if (resource.exists()) {
                ServerResponse.ok()
                    .header("Content-Type", "text/css")
                    .header("Cache-Control", "max-age=3600")
                    .bodyValue(resource)
            } else {
                ServerResponse.notFound().build()
            }
        }

        // 커스텀 JavaScript 파일 라우팅
        GET("/swagger-ui-custom.js") {
            val resource = ClassPathResource("static/swagger-ui-custom.js")
            if (resource.exists()) {
                ServerResponse.ok()
                    .header("Content-Type", "application/javascript")
                    .header("Cache-Control", "max-age=3600")
                    .bodyValue(resource)
            } else {
                ServerResponse.notFound().build()
            }
        }

        // 기본 정적 리소스 라우팅
        GET("/static/**") { request ->
            val path = request.path().substring("/static/".length)
            val resource = ClassPathResource("static/$path")
            if (resource.exists()) {
                val contentType = when (resource.filename?.substringAfterLast('.')) {
                    "css" -> "text/css"
                    "js" -> "application/javascript"
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "gif" -> "image/gif"
                    "svg" -> "image/svg+xml"
                    "ico" -> "image/x-icon"
                    "woff" -> "font/woff"
                    "woff2" -> "font/woff2"
                    "ttf" -> "font/ttf"
                    "eot" -> "application/vnd.ms-fontobject"
                    else -> "application/octet-stream"
                }
                ServerResponse.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "max-age=3600")
                    .bodyValue(resource)
            } else {
                ServerResponse.notFound().build()
            }
        }
    }
}

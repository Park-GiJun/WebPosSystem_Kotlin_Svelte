package com.gijun.backend.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Duration

@Configuration
@EnableWebFlux
class WebConfiguration : WebFluxConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 정적 리소스 서빙
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))

        // Swagger UI 커스텀 파일들
        registry.addResourceHandler("/swagger-ui-custom.css", "/swagger-ui-custom.js")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)))
    }
}
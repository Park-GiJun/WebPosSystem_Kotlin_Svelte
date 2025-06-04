package com.gijun.backend.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
    @Value("\${api.license.url:https://opensource.org/licenses/MIT}") private val licenseUrl: String
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(apiInfo())
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
                            .description("JWT Authorization header using the Bearer scheme")
                    )
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title(apiTitle)
            .description(apiDescription)
            .version(apiVersion)
            .contact(
                Contact()
                    .name(contactName)
                    .email(contactEmail)
                    .url(contactUrl)
            )
            .license(
                License()
                    .name(licenseName)
                    .url(licenseUrl)
            )
    }
}

package com.gijun.backend.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    DatabaseProperties::class,
    RedisProperties::class,
    JwtProperties::class,
    ApiProperties::class
)
class EnvironmentConfiguration

@ConfigurationProperties(prefix = "spring.r2dbc")
data class DatabaseProperties(
    val url: String = "",
    val username: String = "root",
    val password: String = "password"
)

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String = ""
)

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String = "your-secret-key",
    val expiration: Long = 86400000
)

@ConfigurationProperties(prefix = "api")
data class ApiProperties(
    val title: String = "WebPos API",
    val description: String = "Hexagonal Architecture based WebPos Server API",
    val version: String = "1.0.0",
    val contact: ContactProperties = ContactProperties(),
    val license: LicenseProperties = LicenseProperties()
) {
    data class ContactProperties(
        val name: String = "WebPos Team",
        val email: String = "support@webpos.com",
        val url: String = "https://github.com/yourusername/webpos"
    )
    
    data class LicenseProperties(
        val name: String = "MIT License",
        val url: String = "https://opensource.org/licenses/MIT"
    )
}

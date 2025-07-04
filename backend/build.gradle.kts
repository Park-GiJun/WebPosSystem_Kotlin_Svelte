plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "10.10.0"
}

group = "com.gijun"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // Database
    implementation("io.r2dbc:r2dbc-pool")
    implementation("io.asyncer:r2dbc-mysql:1.0.5")
    implementation("mysql:mysql-connector-java:8.0.33")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")

    // ArchUnit for architecture tests
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("webpos.jar")
}

// Flyway 설정 - 환경별 분기
flyway {
    val isProduction = System.getenv("SPRING_PROFILES_ACTIVE") == "prod"
    
    if (isProduction) {
        // Production 환경 설정
        url = "jdbc:mysql://${System.getenv("PRODUCTION_DB_HOST") ?: "210.121.177.150"}:${System.getenv("PRODUCTION_DB_PORT") ?: "3306"}/${System.getenv("PRODUCTION_DB_NAME") ?: "webpossystem"}?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true"
        user = System.getenv("PRODUCTION_DB_USERNAME") ?: "gijunpark"
        password = System.getenv("PRODUCTION_DB_PASSWORD") ?: "park9832"
    } else {
        // Local/Dev 환경 설정
        url = "jdbc:mysql://${System.getenv("DB_HOST") ?: "localhost"}:${System.getenv("DB_PORT") ?: "3306"}/${System.getenv("DB_NAME") ?: "webpos"}?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true"
        user = System.getenv("DB_USERNAME") ?: "root"
        password = System.getenv("DB_PASSWORD") ?: "password"
    }
    
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
    validateOnMigrate = true
    cleanDisabled = isProduction
}

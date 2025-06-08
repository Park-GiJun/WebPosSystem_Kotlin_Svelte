package com.gijun.backend.adapter.`in`.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    @GetMapping
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "timestamp" to java.time.LocalDateTime.now().toString()
        )
    }
}

package com.gijun.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
    // KST 문제 해결을 위한 타임존 강제 설정
    System.setProperty("user.timezone", "Asia/Seoul")
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    
    runApplication<BackendApplication>(*args)
}

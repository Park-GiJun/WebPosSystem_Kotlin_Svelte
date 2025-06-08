package com.gijun.backend.domain.user.vo

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "사용자 ID는 필수입니다." }
    }

    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
        fun fromString(value: String): UserId = UserId(value)
    }
}

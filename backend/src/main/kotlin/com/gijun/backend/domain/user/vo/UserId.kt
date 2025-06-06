package com.gijun.backend.domain.user.vo

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "사용자 ID는 필수입니다." }
        require(isValidUuid(value)) { "올바른 UUID 형식이 아닙니다." }
    }

    private fun isValidUuid(value: String): Boolean {
        return try {
            UUID.fromString(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
        fun fromString(value: String): UserId = UserId(value)
    }
}
package com.gijun.backend.domain.user.vo

@JvmInline
value class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "패스워드 해시는 필수입니다." }
        require(value.length >= 60) { "패스워드 해시가 너무 짧습니다." }
    }

    companion object {
        fun fromString(value: String): PasswordHash = PasswordHash(value)
    }
}
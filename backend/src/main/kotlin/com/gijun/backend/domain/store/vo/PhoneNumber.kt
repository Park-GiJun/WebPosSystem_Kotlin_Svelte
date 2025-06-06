package com.gijun.backend.domain.store.vo

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "전화번호는 필수입니다." }
        require(isValidFormat(value)) { "전화번호 형식이 올바르지 않습니다." }
    }

    private fun isValidFormat(value: String): Boolean {
        // 000-0000-0000 또는 00-0000-0000 형식
        return value.matches(Regex("\\d{2,3}-\\d{3,4}-\\d{4}"))
    }

    companion object {
        fun fromString(value: String): PhoneNumber = PhoneNumber(value)
    }
}
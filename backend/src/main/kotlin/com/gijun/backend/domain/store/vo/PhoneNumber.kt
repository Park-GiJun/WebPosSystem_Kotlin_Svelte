package com.gijun.backend.domain.store.vo

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "전화번호는 필수입니다." }
        require(isValidFormat(value)) { "전화번호 형식이 올바르지 않습니다. 입력값: $value" }
    }

    private fun isValidFormat(value: String): Boolean {
        // 다양한 전화번호 형식 허용
        return value.matches(Regex("\\d{2,3}-\\d{3,4}-\\d{4}")) ||  // 000-0000-0000
               value.matches(Regex("\\d{10,11}")) ||                // 숫자만 10-11자리
               value.matches(Regex("\\(\\d{2,3}\\)\\d{3,4}-\\d{4}")) // (000)0000-0000
    }

    companion object {
        fun fromString(value: String): PhoneNumber = PhoneNumber(value)
        
        fun fromStringOrNull(value: String?): PhoneNumber? {
            return if (value.isNullOrBlank()) null else try {
                PhoneNumber(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}
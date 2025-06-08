package com.gijun.backend.domain.store.vo

@JvmInline
value class BusinessLicense(val value: String) {
    init {
        require(value.isNotBlank()) { "사업자등록번호는 필수입니다." }
        // 유효성 검증을 완화 - 다양한 형식 허용
        require(isValidFormat(value)) { "사업자등록번호 형식이 올바르지 않습니다. 입력값: $value" }
    }

    private fun isValidFormat(value: String): Boolean {
        // 000-00-00000 형식 또는 10자리 숫자 허용
        return value.matches(Regex("\\d{3}-\\d{2}-\\d{5}")) || 
               value.matches(Regex("\\d{10}"))
    }

    companion object {
        fun fromString(value: String): BusinessLicense = BusinessLicense(value)
        
        fun fromStringOrNull(value: String?): BusinessLicense? {
            return if (value.isNullOrBlank()) null else try {
                BusinessLicense(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}

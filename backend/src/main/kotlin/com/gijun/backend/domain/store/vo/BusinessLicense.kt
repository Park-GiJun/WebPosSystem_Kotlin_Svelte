package com.gijun.backend.domain.store.vo

@JvmInline
value class BusinessLicense(val value: String) {
    init {
        require(value.isNotBlank()) { "사업자등록번호는 필수입니다." }
        require(isValidFormat(value)) { "사업자등록번호 형식이 올바르지 않습니다." }
    }

    private fun isValidFormat(value: String): Boolean {
        // 000-00-00000 형식 검증
        return value.matches(Regex("\\d{3}-\\d{2}-\\d{5}"))
    }

    companion object {
        fun fromString(value: String): BusinessLicense = BusinessLicense(value)
    }
}

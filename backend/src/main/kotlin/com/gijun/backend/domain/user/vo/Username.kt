package com.gijun.backend.domain.user.vo

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "사용자명은 필수입니다." }
        require(value.length in 3..50) { "사용자명은 3자 이상 50자 이하여야 합니다." }
        require(isValidFormat(value)) { "사용자명은 영문, 숫자, 언더스코어, 하이픈만 사용 가능합니다." }
    }

    private fun isValidFormat(value: String): Boolean {
        return value.matches(Regex("^[a-zA-Z0-9_-]+$"))
    }

    companion object {
        fun fromString(value: String): Username = Username(value.trim().lowercase())
    }
}

package com.gijun.backend.domain.user.vo

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "사용자명은 필수입니다." }
        require(value.length >= 3) { "사용자명은 최소 3자 이상이어야 합니다." }
        require(value.length <= 50) { "사용자명은 최대 50자까지 가능합니다." }
        require(isValidFormat(value)) { "사용자명은 영문자, 숫자, 언더스코어만 사용 가능합니다: $value" }
    }

    private fun isValidFormat(username: String): Boolean {
        val usernameRegex = "^[a-zA-Z0-9_]+$"
        return username.matches(usernameRegex.toRegex())
    }

    companion object {
        fun fromString(value: String): Username = Username(value)
        
        fun fromStringOrNull(value: String?): Username? {
            return if (value.isNullOrBlank()) null else try {
                Username(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}

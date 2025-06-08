package com.gijun.backend.domain.user.vo

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "이메일은 필수입니다." }
        require(isValidFormat(value)) { "올바른 이메일 형식이 아닙니다: $value" }
    }

    private fun isValidFormat(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    companion object {
        fun fromString(value: String): Email = Email(value)
        
        fun fromStringOrNull(value: String?): Email? {
            return if (value.isNullOrBlank()) null else try {
                Email(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}

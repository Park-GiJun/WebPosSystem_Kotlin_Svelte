package com.gijun.backend.domain.user.vo

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "이메일은 필수입니다." }
        require(isValidFormat(value)) { "올바른 이메일 형식이 아닙니다." }
        require(value.length <= 320) { "이메일은 320자를 초과할 수 없습니다." }
    }

    private fun isValidFormat(value: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(value)
    }

    fun getDomain(): String = value.substringAfter("@")

    fun getLocalPart(): String = value.substringBefore("@")

    companion object {
        fun fromString(value: String): Email = Email(value.trim().lowercase())
    }
}
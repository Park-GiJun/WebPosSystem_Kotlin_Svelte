package com.gijun.backend.domain.user.enums

enum class UserStatus(val description: String) {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지"),
    PENDING_VERIFICATION("인증 대기"),
    LOCKED("잠김");

    fun isOperational(): Boolean = this == ACTIVE
    fun canLogin(): Boolean = this in listOf(ACTIVE, PENDING_VERIFICATION)
    fun isBlocked(): Boolean = this in listOf(SUSPENDED, LOCKED)
}
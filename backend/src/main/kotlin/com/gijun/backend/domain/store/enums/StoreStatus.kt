package com.gijun.backend.domain.store.enums

enum class StoreStatus(val description: String) {
    ACTIVE("운영중"),
    INACTIVE("운영중지"),
    SUSPENDED("운영정지"),
    CLOSED("폐점");

    fun isOperating(): Boolean = this == ACTIVE
    fun isClosed(): Boolean = this == CLOSED
    fun isSuspended(): Boolean = this == SUSPENDED
}
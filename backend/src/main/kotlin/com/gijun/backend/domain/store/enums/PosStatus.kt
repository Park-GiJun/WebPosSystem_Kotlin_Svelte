package com.gijun.backend.domain.store.enums

enum class PosStatus(val description: String) {
    ACTIVE("정상"),
    INACTIVE("비활성"),
    MAINTENANCE("점검중");

    fun isOperating(): Boolean = this == ACTIVE
    fun needsMaintenance(): Boolean = this == MAINTENANCE
}
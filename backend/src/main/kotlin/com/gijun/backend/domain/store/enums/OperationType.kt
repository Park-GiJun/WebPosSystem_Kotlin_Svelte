package com.gijun.backend.domain.store.enums

enum class OperationType(val description: String) {
    DIRECT("직영점"),
    FRANCHISE("가맹점");

    fun isDirect(): Boolean = this == DIRECT
    fun isFranchise(): Boolean = this == FRANCHISE
}
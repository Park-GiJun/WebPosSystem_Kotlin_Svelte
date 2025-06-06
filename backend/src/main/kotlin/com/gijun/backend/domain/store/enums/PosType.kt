package com.gijun.backend.domain.store.enums

enum class PosType(val description: String) {
    MAIN("메인 POS"),
    SUB("서브 POS"),
    MOBILE("모바일 POS");

    fun isMain(): Boolean = this == MAIN
}
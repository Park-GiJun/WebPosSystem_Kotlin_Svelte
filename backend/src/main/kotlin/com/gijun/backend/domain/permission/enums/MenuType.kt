package com.gijun.backend.domain.permission.enums

enum class MenuType(val description: String) {
    CATEGORY("카테고리"),   // 카테고리 (메뉴 그룹)
    MENU("메뉴"),          // 일반 메뉴
    FUNCTION("기능")       // 기능
}

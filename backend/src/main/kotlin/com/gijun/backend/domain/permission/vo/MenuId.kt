package com.gijun.backend.domain.permission.vo

import java.util.UUID

@JvmInline
value class MenuId(val value: String) {
    init {
        require(value.isNotBlank()) { "메뉴 ID는 필수입니다." }
    }

    companion object {
        fun generate(): MenuId = MenuId(UUID.randomUUID().toString())
        fun fromString(value: String): MenuId = MenuId(value)
    }
}
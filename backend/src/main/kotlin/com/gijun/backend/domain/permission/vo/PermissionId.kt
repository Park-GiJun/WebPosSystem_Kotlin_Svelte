package com.gijun.backend.domain.permission.vo

import java.util.UUID

@JvmInline
value class PermissionId(val value: String) {
    init {
        require(value.isNotBlank()) { "권한 ID는 필수입니다." }
    }

    companion object {
        fun generate(): PermissionId = PermissionId(UUID.randomUUID().toString())
        fun fromString(value: String): PermissionId = PermissionId(value)
    }
}
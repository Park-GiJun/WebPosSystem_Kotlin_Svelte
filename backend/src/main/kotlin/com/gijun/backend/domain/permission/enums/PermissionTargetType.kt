package com.gijun.backend.domain.permission.enums

enum class PermissionTargetType(val description: String) {
    USER("사용자"),
    ROLE("역할"),
    ORGANIZATION("조직")
}

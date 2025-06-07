package com.gijun.backend.domain.permission.enums

enum class PermissionType(val description: String) {
    READ("읽기"),        // 읽기 권한
    WRITE("쓰기"),       // 쓰기 권한  
    DELETE("삭제"),      // 삭제 권한
    ADMIN("관리자")      // 관리자 권한 (모든 권한 포함)
}

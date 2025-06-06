package com.gijun.backend.domain.user.enums

enum class UserRole(val description: String, val level: Int) {
    USER("일반 사용자", 1),
    STORE_MANAGER("매장 관리자", 2),
    AREA_MANAGER("지역 관리자", 3),
    HQ_MANAGER("본사 관리자", 4),
    SYSTEM_ADMIN("시스템 관리자", 5),
    SUPER_ADMIN("최고 관리자", 6);

    fun isAdmin(): Boolean = this in listOf(SYSTEM_ADMIN, SUPER_ADMIN)
    fun isManager(): Boolean = this in listOf(STORE_MANAGER, AREA_MANAGER, HQ_MANAGER)
    fun hasHigherOrEqualLevel(other: UserRole): Boolean = this.level >= other.level
    fun canManage(other: UserRole): Boolean = this.level > other.level
}

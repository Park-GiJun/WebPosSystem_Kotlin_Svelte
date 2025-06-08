package com.gijun.backend.domain.user.enums

enum class UserRole(val description: String, val level: Int) {
    USER("일반 사용자", 1),
    STORE_ADMIN("매장 관리자", 2),
    HEADQUARTERS_ADMIN("본사 관리자", 3),
    ADMIN("시스템 관리자", 4),
    SUPER_ADMIN("최고 관리자", 5),
    
    // 레거시 호환성을 위한 별칭들
    STORE_MANAGER("매장 관리자", 2),
    AREA_MANAGER("지역 관리자", 3),
    HQ_MANAGER("본사 관리자", 3),
    SYSTEM_ADMIN("시스템 관리자", 4);

    fun isAdmin(): Boolean = this in listOf(ADMIN, SYSTEM_ADMIN, SUPER_ADMIN)
    fun isManager(): Boolean = this in listOf(STORE_ADMIN, STORE_MANAGER, HEADQUARTERS_ADMIN, HQ_MANAGER, AREA_MANAGER)
    fun hasHigherOrEqualLevel(other: UserRole): Boolean = this.level >= other.level
    fun canManage(other: UserRole): Boolean = this.level > other.level
}

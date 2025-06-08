package com.gijun.backend.adapter.`in`.web.dto.permission

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "내 메뉴 권한 응답")
data class MyMenusResponse(
    @Schema(description = "접근 가능한 메뉴 목록")
    val menus: List<MenuDto>,
    
    @Schema(description = "메뉴별 권한 정보")
    val permissions: List<PermissionDto>
)

@Schema(description = "메뉴 정보")
data class MenuDto(
    @Schema(description = "메뉴 ID", example = "menu-001")
    val menuId: String,
    
    @Schema(description = "메뉴 코드", example = "DASHBOARD")
    val menuCode: String,
    
    @Schema(description = "메뉴명", example = "대시보드")
    val menuName: String,
    
    @Schema(description = "메뉴 경로", example = "/dashboard")
    val menuPath: String,
    
    @Schema(description = "부모 메뉴 ID")
    val parentMenuId: String?,
    
    @Schema(description = "메뉴 레벨", example = "1")
    val menuLevel: Int,
    
    @Schema(description = "표시 순서", example = "1")
    val displayOrder: Int,
    
    @Schema(description = "아이콘명", example = "dashboard")
    val iconName: String?,
    
    @Schema(description = "메뉴 타입", example = "MENU")
    val menuType: String
)

@Schema(description = "권한 정보")
data class PermissionDto(
    @Schema(description = "메뉴 코드", example = "DASHBOARD")
    val menuCode: String,
    
    @Schema(description = "권한 타입", example = "READ")
    val permissionType: String,
    
    @Schema(description = "읽기 권한", example = "true")
    val hasRead: Boolean,
    
    @Schema(description = "쓰기 권한", example = "false")
    val hasWrite: Boolean,
    
    @Schema(description = "삭제 권한", example = "false")
    val hasDelete: Boolean,
    
    @Schema(description = "관리자 권한", example = "false")
    val hasAdmin: Boolean
)

@Schema(description = "권한 확인 요청")
data class CheckPermissionRequest(
    @Schema(description = "메뉴 코드", example = "ADMIN_USERS", required = true)
    val menuCode: String,
    
    @Schema(description = "확인할 권한 타입", example = "WRITE", required = true)
    val requiredPermission: String
)

@Schema(description = "권한 확인 응답")
data class CheckPermissionResponse(
    @Schema(description = "권한 보유 여부", example = "true")
    val hasPermission: Boolean,
    
    @Schema(description = "권한 확인 메시지", example = "ADMIN_USERS 메뉴에 대한 WRITE 권한이 있습니다")
    val message: String? = null,
    
    @Schema(description = "권한 레벨", example = "ADMIN")
    val permissionLevel: String? = null,
    
    @Schema(description = "권한 부여 시간")
    val grantedAt: LocalDateTime? = null
)

package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.permission.*
import com.gijun.backend.application.service.PermissionService
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.permission.enums.PermissionType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(
    name = "🔐 Permissions", 
    description = "권한 및 메뉴 관리 API"
)
class PermissionController(
    private val jwtUtil: JwtUtil,
    private val permissionService: PermissionService
) {

    @GetMapping("/my-menus")
    @Operation(
        summary = "📋 내 메뉴 권한 조회",
        description = """
            **현재 로그인한 사용자가 접근 가능한 메뉴와 권한을 조회합니다.**
            
            📋 **조회 정보:**
            - 🗂️ **접근 가능한 메뉴 목록**: 사용자 역할에 따른 메뉴 트리
            - 🔐 **메뉴별 권한**: 읽기/쓰기/삭제/관리 권한 상세
            - 🎨 **UI 정보**: 아이콘, 경로, 정렬 순서 등
            
            🌳 **메뉴 구조:**
            - 계층형 구조로 제공 (parent-child 관계)
            - 레벨별 정렬 및 표시 순서 포함
            - 메뉴 타입별 분류 (MENU, BUTTON, FUNCTION)
            
            🔐 **권한 타입:**
            - **READ**: 조회 권한
            - **WRITE**: 생성/수정 권한  
            - **DELETE**: 삭제 권한
            - **ADMIN**: 관리자 권한
            
            💡 **활용:**
            - 프론트엔드 메뉴 렌더링
            - 동적 권한 체크
            - UI 컴포넌트 표시/숨김 제어
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🔐 Permissions"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 메뉴 권한 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MyMenusResponse::class),
                    examples = [
                        ExampleObject(
                            name = "관리자 메뉴",
                            summary = "시스템 관리자가 조회하는 전체 메뉴",
                            value = """{
                                "menus": [
                                    {
                                        "menuId": "menu-001",
                                        "menuCode": "DASHBOARD",
                                        "menuName": "대시보드",
                                        "menuPath": "/dashboard",
                                        "parentMenuId": null,
                                        "menuLevel": 1,
                                        "displayOrder": 1,
                                        "iconName": "dashboard",
                                        "menuType": "MENU"
                                    },
                                    {
                                        "menuId": "menu-002",
                                        "menuCode": "ADMIN_USERS",
                                        "menuName": "사용자 관리",
                                        "menuPath": "/admin/users",
                                        "parentMenuId": "menu-admin",
                                        "menuLevel": 2,
                                        "displayOrder": 1,
                                        "iconName": "users",
                                        "menuType": "MENU"
                                    }
                                ],
                                "permissions": [
                                    {
                                        "menuCode": "DASHBOARD",
                                        "permissionType": "READ",
                                        "hasRead": true,
                                        "hasWrite": false,
                                        "hasDelete": false,
                                        "hasAdmin": false
                                    },
                                    {
                                        "menuCode": "ADMIN_USERS",
                                        "permissionType": "ADMIN",
                                        "hasRead": true,
                                        "hasWrite": true,
                                        "hasDelete": true,
                                        "hasAdmin": true
                                    }
                                ]
                            }"""
                        ),
                        ExampleObject(
                            name = "일반 사용자 메뉴",
                            summary = "일반 사용자가 조회하는 제한된 메뉴",
                            value = """{
                                "menus": [
                                    {
                                        "menuId": "menu-001",
                                        "menuCode": "DASHBOARD",
                                        "menuName": "대시보드",
                                        "menuPath": "/dashboard",
                                        "parentMenuId": null,
                                        "menuLevel": 1,
                                        "displayOrder": 1,
                                        "iconName": "dashboard",
                                        "menuType": "MENU"
                                    }
                                ],
                                "permissions": [
                                    {
                                        "menuCode": "DASHBOARD",
                                        "permissionType": "READ",
                                        "hasRead": true,
                                        "hasWrite": false,
                                        "hasDelete": false,
                                        "hasAdmin": false
                                    }
                                ]
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "유효하지 않은 토큰입니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun getMyMenus(
        @Parameter(
            description = "JWT 인증 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<MyMenusResponse> {
        val token = authorization.removePrefix("Bearer ")
        
        try {
            val username = jwtUtil.getUsernameFromToken(token)
            
            // 실제 데이터베이스에서 사용자 메뉴 권한 조회
            val userMenuPermissions = permissionService.getUserMenus(username)
            
            val menus = userMenuPermissions.map { userMenuPermission ->
                MenuDto(
                    menuId = userMenuPermission.menuId,
                    menuCode = userMenuPermission.menuCode,
                    menuName = userMenuPermission.menuName,
                    menuPath = userMenuPermission.menuPath,
                    parentMenuId = userMenuPermission.parentMenuId,
                    menuLevel = userMenuPermission.menuLevel,
                    displayOrder = userMenuPermission.displayOrder,
                    iconName = userMenuPermission.iconName,
                    menuType = userMenuPermission.menuType
                )
            }

            val permissions = userMenuPermissions.map { userMenuPermission ->
                PermissionDto(
                    menuCode = userMenuPermission.menuCode,
                    permissionType = userMenuPermission.permissionType,
                    hasRead = userMenuPermission.hasReadPermission,
                    hasWrite = userMenuPermission.hasWritePermission,
                    hasDelete = userMenuPermission.hasDeletePermission,
                    hasAdmin = userMenuPermission.hasAdminPermission
                )
            }
            
            val response = MyMenusResponse(menus, permissions)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.status(401).build()
        }
    }

    @PostMapping("/check")
    @Operation(
        summary = "🔍 권한 확인",
        description = """
            **특정 메뉴에 대한 사용자의 권한을 실시간으로 확인합니다.**
            
            🔐 **권한 확인 프로세스:**
            1. JWT 토큰에서 사용자 정보 추출
            2. 요청된 메뉴 코드와 권한 타입 검증
            3. 사용자의 역할과 권한 매트릭스 조회
            4. 권한 보유 여부 반환
            
            📋 **확인 가능한 권한:**
            - **READ**: 조회/읽기 권한
            - **WRITE**: 생성/수정 권한
            - **DELETE**: 삭제 권한
            - **ADMIN**: 관리자 권한
            
            💡 **활용 사례:**
            - 버튼/링크 활성화 여부 결정
            - API 호출 전 사전 권한 체크
            - 동적 UI 렌더링
            - 보안 강화를 위한 이중 체크
            
            ⚡ **실시간 체크:**
            - 권한 변경 시 즉시 반영
            - 세션 기반 캐싱으로 성능 최적화
            - 로그 기록으로 감사 추적
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["🔐 Permissions"]
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "✅ 권한 확인 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CheckPermissionResponse::class),
                    examples = [
                        ExampleObject(
                            name = "권한 있음",
                            summary = "사용자가 해당 권한을 보유한 경우",
                            value = """{
                                "hasPermission": true,
                                "message": "ADMIN_USERS 메뉴에 대한 WRITE 권한이 있습니다",
                                "permissionLevel": "ADMIN",
                                "grantedAt": "2025-01-01T00:00:00"
                            }"""
                        ),
                        ExampleObject(
                            name = "권한 없음",
                            summary = "사용자가 해당 권한을 보유하지 않은 경우",
                            value = """{
                                "hasPermission": false,
                                "message": "ADMIN_USERS 메뉴에 대한 DELETE 권한이 없습니다",
                                "permissionLevel": null,
                                "grantedAt": null
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "❌ 잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "잘못된 권한 타입",
                            value = """{
                                "success": false,
                                "message": "지원하지 않는 권한 타입입니다: INVALID_PERMISSION",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        ),
                        ExampleObject(
                            name = "존재하지 않는 메뉴",
                            value = """{
                                "success": false,
                                "message": "존재하지 않는 메뉴 코드입니다: INVALID_MENU",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "🚫 인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            value = """{
                                "success": false,
                                "message": "유효하지 않은 토큰입니다",
                                "timestamp": "2025-06-07T21:00:00"
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    suspend fun checkPermission(
        @Parameter(
            description = "JWT 인증 토큰", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
        )
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: CheckPermissionRequest
    ): ResponseEntity<CheckPermissionResponse> {
        val token = authorization.removePrefix("Bearer ")
        
        try {
            val username = jwtUtil.getUsernameFromToken(token)
            val requiredPermission = PermissionType.valueOf(request.requiredPermission)
            
            val hasPermission = permissionService.checkUserPermission(
                username, 
                request.menuCode, 
                requiredPermission
            )
            
            return ResponseEntity.ok(CheckPermissionResponse(
                hasPermission = hasPermission,
                message = if (hasPermission) 
                    "${request.menuCode} 메뉴에 대한 ${request.requiredPermission} 권한이 있습니다"
                else 
                    "${request.menuCode} 메뉴에 대한 ${request.requiredPermission} 권한이 없습니다",
                permissionLevel = if (hasPermission) requiredPermission.name else null,
                grantedAt = if (hasPermission) java.time.LocalDateTime.now() else null
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(401).build()
        }
    }
}

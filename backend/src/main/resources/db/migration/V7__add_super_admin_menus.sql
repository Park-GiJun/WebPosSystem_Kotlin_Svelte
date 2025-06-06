-- V7: 슈퍼 어드민 메뉴 추가
-- 매장 관리 및 POS 관리 메뉴를 어드민 시스템에 추가

-- 매장 관리 메뉴 추가
INSERT INTO menus (
    menu_id, 
    menu_code, 
    menu_name, 
    menu_path, 
    parent_menu_id, 
    menu_type, 
    menu_level,
    display_order, 
    icon_name, 
    description, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    'menu-admin-stores',
    'ADMIN_STORES',
    '매장 관리',
    '/admin/stores',
    'menu-admin',
    'MENU',
    2,
    200,
    'building',
    '전체 매장을 관리합니다',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    menu_level = 2,
    display_order = 200,
    updated_at = NOW();

-- POS 관리 메뉴 추가
INSERT INTO menus (
    menu_id, 
    menu_code, 
    menu_name, 
    menu_path, 
    parent_menu_id, 
    menu_type, 
    menu_level,
    display_order, 
    icon_name, 
    description, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    'menu-admin-pos',
    'ADMIN_POS',
    'POS 관리',
    '/admin/pos',
    'menu-admin',
    'MENU',
    2,
    300,
    'server',
    '전체 POS 시스템을 관리합니다',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    menu_level = 2,
    display_order = 300,
    updated_at = NOW();

-- 매장 관리 메뉴 권한 추가
INSERT INTO permissions (
    permission_id,
    menu_id,
    target_type,
    target_id,
    permission_type,
    granted_by,
    is_active,
    created_at,
    updated_at
) VALUES 
-- 매장 관리 - 읽기 권한
(
    'perm-admin-stores-read',
    'menu-admin-stores',
    'ROLE',
    'SUPER_ADMIN',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 쓰기 권한
(
    'perm-admin-stores-write',
    'menu-admin-stores',
    'ROLE',
    'SUPER_ADMIN',
    'WRITE',
    'system',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 삭제 권한
(
    'perm-admin-stores-delete',
    'menu-admin-stores',
    'ROLE',
    'SUPER_ADMIN',
    'DELETE',
    'system',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 관리자 권한
(
    'perm-admin-stores-admin',
    'menu-admin-stores',
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    is_active = true,
    updated_at = NOW();

-- POS 관리 메뉴 권한 추가
INSERT INTO permissions (
    permission_id,
    menu_id,
    target_type,
    target_id,
    permission_type,
    granted_by,
    is_active,
    created_at,
    updated_at
) VALUES 
-- POS 관리 - 읽기 권한
(
    'perm-admin-pos-read',
    'menu-admin-pos',
    'ROLE',
    'SUPER_ADMIN',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 쓰기 권한
(
    'perm-admin-pos-write',
    'menu-admin-pos',
    'ROLE',
    'SUPER_ADMIN',
    'WRITE',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 삭제 권한
(
    'perm-admin-pos-delete',
    'menu-admin-pos',
    'ROLE',
    'SUPER_ADMIN',
    'DELETE',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 관리자 권한
(
    'perm-admin-pos-admin',
    'menu-admin-pos',
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    is_active = true,
    updated_at = NOW();

-- 시스템 어드민 역할에도 읽기/쓰기 권한 부여 (삭제 권한 제외)
INSERT INTO permissions (
    permission_id,
    menu_id,
    target_type,
    target_id,
    permission_type,
    granted_by,
    is_active,
    created_at,
    updated_at
) VALUES 
-- 매장 관리 - 시스템 어드민 읽기 권한
(
    'perm-admin-stores-sysadmin-read',
    'menu-admin-stores',
    'ROLE',
    'SYSTEM_ADMIN',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 시스템 어드민 쓰기 권한
(
    'perm-admin-stores-sysadmin-write',
    'menu-admin-stores',
    'ROLE',
    'SYSTEM_ADMIN',
    'WRITE',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 시스템 어드민 읽기 권한
(
    'perm-admin-pos-sysadmin-read',
    'menu-admin-pos',
    'ROLE',
    'SYSTEM_ADMIN',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 시스템 어드민 쓰기 권한
(
    'perm-admin-pos-sysadmin-write',
    'menu-admin-pos',
    'ROLE',
    'SYSTEM_ADMIN',
    'WRITE',
    'system',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    is_active = true,
    updated_at = NOW();

-- 본사 관리자 역할에는 읽기 권한만 부여
INSERT INTO permissions (
    permission_id,
    menu_id,
    target_type,
    target_id,
    permission_type,
    granted_by,
    is_active,
    created_at,
    updated_at
) VALUES 
-- 매장 관리 - 본사 관리자 읽기 권한
(
    'perm-admin-stores-hqmgr-read',
    'menu-admin-stores',
    'ROLE',
    'HQ_MANAGER',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 본사 관리자 읽기 권한
(
    'perm-admin-pos-hqmgr-read',
    'menu-admin-pos',
    'ROLE',
    'HQ_MANAGER',
    'READ',
    'system',
    true,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    is_active = true,
    updated_at = NOW();

-- 메뉴 순서 조정 (기존 메뉴들의 display_order 업데이트)
UPDATE menus 
SET display_order = 100, updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET display_order = 150, updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

UPDATE menus 
SET display_order = 400, updated_at = NOW()
WHERE menu_code = 'ADMIN_SYSTEM';

UPDATE menus 
SET display_order = 500, updated_at = NOW()
WHERE menu_code = 'ADMIN_AUDIT';

-- 메뉴 설명 업데이트
UPDATE menus 
SET description = '시스템 사용자 계정을 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET description = '시스템 권한 및 역할을 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

UPDATE menus 
SET description = '시스템 전반 설정을 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_SYSTEM';

UPDATE menus 
SET description = '시스템 감사 로그를 조회합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_AUDIT';

-- 아이콘 업데이트 (기존 메뉴들)
UPDATE menus 
SET icon_name = 'users', updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET icon_name = 'key', updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

UPDATE menus 
SET icon_name = 'cog', updated_at = NOW()
WHERE menu_code = 'ADMIN_SYSTEM';

UPDATE menus 
SET icon_name = 'document-text', updated_at = NOW()
WHERE menu_code = 'ADMIN_AUDIT';

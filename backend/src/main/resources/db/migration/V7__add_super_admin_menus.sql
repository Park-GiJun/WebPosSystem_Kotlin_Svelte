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
    200,
    'building',
    '전체 매장을 관리합니다',
    true,
    NOW(),
    NOW()
);

-- POS 관리 메뉴 추가
INSERT INTO menus (
    menu_id, 
    menu_code, 
    menu_name, 
    menu_path, 
    parent_menu_id, 
    menu_type, 
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
    300,
    'server',
    '전체 POS 시스템을 관리합니다',
    true,
    NOW(),
    NOW()
);

-- 매장 관리 메뉴 권한 추가
INSERT INTO permissions (
    permission_id,
    menu_id,
    permission_type,
    permission_name,
    permission_code,
    description,
    is_active,
    created_at,
    updated_at
) VALUES 
-- 매장 관리 - 읽기 권한
(
    'perm-admin-stores-read',
    'menu-admin-stores',
    'READ',
    '매장 관리 조회',
    'ADMIN_STORES_READ',
    '매장 목록 조회 및 상세 정보 확인',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 쓰기 권한
(
    'perm-admin-stores-write',
    'menu-admin-stores',
    'WRITE',
    '매장 관리 편집',
    'ADMIN_STORES_WRITE',
    '매장 생성, 수정, 상태 변경',
    true,
    NOW(),
    NOW()
),
-- 매장 관리 - 삭제 권한
(
    'perm-admin-stores-delete',
    'menu-admin-stores',
    'DELETE',
    '매장 관리 삭제',
    'ADMIN_STORES_DELETE',
    '매장 삭제 및 완전 제거',
    true,
    NOW(),
    NOW()
);

-- POS 관리 메뉴 권한 추가
INSERT INTO permissions (
    permission_id,
    menu_id,
    permission_type,
    permission_name,
    permission_code,
    description,
    is_active,
    created_at,
    updated_at
) VALUES 
-- POS 관리 - 읽기 권한
(
    'perm-admin-pos-read',
    'menu-admin-pos',
    'READ',
    'POS 관리 조회',
    'ADMIN_POS_READ',
    'POS 목록 조회 및 상세 정보 확인',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 쓰기 권한
(
    'perm-admin-pos-write',
    'menu-admin-pos',
    'WRITE',
    'POS 관리 편집',
    'ADMIN_POS_WRITE',
    'POS 생성, 수정, 설정 변경',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 삭제 권한
(
    'perm-admin-pos-delete',
    'menu-admin-pos',
    'DELETE',
    'POS 관리 삭제',
    'ADMIN_POS_DELETE',
    'POS 삭제 및 완전 제거',
    true,
    NOW(),
    NOW()
),
-- POS 관리 - 점검 권한
(
    'perm-admin-pos-maintenance',
    'menu-admin-pos',
    'EXECUTE',
    'POS 점검 관리',
    'ADMIN_POS_MAINTENANCE',
    'POS 점검 시작 및 완료 처리',
    true,
    NOW(),
    NOW()
);

-- 슈퍼 어드민 역할에 새로운 권한들 부여
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT 
    r.role_id, 
    p.permission_id,
    NOW(),
    NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'SUPER_ADMIN' 
AND p.permission_code IN (
    'ADMIN_STORES_READ',
    'ADMIN_STORES_WRITE', 
    'ADMIN_STORES_DELETE',
    'ADMIN_POS_READ',
    'ADMIN_POS_WRITE',
    'ADMIN_POS_DELETE',
    'ADMIN_POS_MAINTENANCE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.role_id 
    AND rp.permission_id = p.permission_id
);

-- 시스템 어드민 역할에도 읽기/쓰기 권한 부여 (삭제 권한 제외)
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT 
    r.role_id, 
    p.permission_id,
    NOW(),
    NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'SYSTEM_ADMIN' 
AND p.permission_code IN (
    'ADMIN_STORES_READ',
    'ADMIN_STORES_WRITE',
    'ADMIN_POS_READ',
    'ADMIN_POS_WRITE',
    'ADMIN_POS_MAINTENANCE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.role_id 
    AND rp.permission_id = p.permission_id
);

-- 본사 관리자 역할에는 읽기 권한만 부여
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT 
    r.role_id, 
    p.permission_id,
    NOW(),
    NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'HQ_MANAGER' 
AND p.permission_code IN (
    'ADMIN_STORES_READ',
    'ADMIN_POS_READ'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.role_id 
    AND rp.permission_id = p.permission_id
);

-- 메뉴 순서 조정 (기존 메뉴들의 display_order 업데이트)
UPDATE menus 
SET display_order = 100, updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET display_order = 400, updated_at = NOW()
WHERE menu_code = 'ADMIN_ORGANIZATIONS';

UPDATE menus 
SET display_order = 500, updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

-- 메뉴 설명 업데이트
UPDATE menus 
SET description = '시스템 사용자 계정을 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET description = '조직 구조 및 부서를 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_ORGANIZATIONS';

UPDATE menus 
SET description = '시스템 권한 및 역할을 관리합니다', updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

-- 아이콘 업데이트 (기존 메뉴들)
UPDATE menus 
SET icon_name = 'users', updated_at = NOW()
WHERE menu_code = 'ADMIN_USERS';

UPDATE menus 
SET icon_name = 'building-office', updated_at = NOW()
WHERE menu_code = 'ADMIN_ORGANIZATIONS';

UPDATE menus 
SET icon_name = 'key', updated_at = NOW()
WHERE menu_code = 'ADMIN_PERMISSIONS';

-- ========================================
-- V6: admin 계정 메뉴 권한 추가
-- ========================================

-- 기본 admin 사용자 생성 (이미 존재할 경우 업데이트)
INSERT INTO users (
    id, 
    username, 
    email, 
    password_hash, 
    roles, 
    user_status, 
    organization_type, 
    organization_id, 
    created_at, 
    updated_at
) VALUES (
    'admin', 
    'admin', 
    'admin@webpos.com', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMye2Bfx9cAYpG5n7DOQHq36yUqn1W.h5ja', -- password: admin
    '["SUPER_ADMIN", "SYSTEM_ADMIN"]', 
    'ACTIVE', 
    'SYSTEM', 
    NULL, 
    NOW(), 
    NOW()
) ON DUPLICATE KEY UPDATE 
    roles = VALUES(roles),
    user_status = VALUES(user_status),
    organization_type = VALUES(organization_type),
    organization_id = VALUES(organization_id),
    updated_at = NOW();

-- admin 사용자에게 모든 메뉴에 대한 ADMIN 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-admin-user-', m.menu_id, '-admin'),
    m.menu_id,
    'USER',
    'admin',
    'ADMIN',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- SUPER_ADMIN 역할에 모든 메뉴 권한 추가 (누락된 권한이 있을 경우)
-- ========================================
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-super-admin-', m.menu_id, '-admin'),
    m.menu_id,
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
AND NOT EXISTS (
    SELECT 1 FROM permissions p 
    WHERE p.menu_id = m.menu_id 
    AND p.target_type = 'ROLE' 
    AND p.target_id = 'SUPER_ADMIN'
    AND p.is_active = TRUE
)
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- SYSTEM_ADMIN 역할에 적절한 권한 추가
-- ========================================
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-system-admin-', m.menu_id, '-admin'),
    m.menu_id,
    'ROLE',
    'SYSTEM_ADMIN',
    'ADMIN',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
AND m.menu_code NOT LIKE 'ADMIN_%'  -- 슈퍼어드민 전용 메뉴 제외
AND NOT EXISTS (
    SELECT 1 FROM permissions p 
    WHERE p.menu_id = m.menu_id 
    AND p.target_type = 'ROLE' 
    AND p.target_id = 'SYSTEM_ADMIN'
    AND p.is_active = TRUE
)
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- HQ_MANAGER 역할에 영업정보시스템 권한 추가
-- ========================================
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-hq-manager-', m.menu_id, '-write'),
    m.menu_id,
    'ROLE',
    'HQ_MANAGER',
    'WRITE',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
AND m.menu_code LIKE 'BUSINESS_%'  -- 영업정보시스템 메뉴만
AND NOT EXISTS (
    SELECT 1 FROM permissions p 
    WHERE p.menu_id = m.menu_id 
    AND p.target_type = 'ROLE' 
    AND p.target_id = 'HQ_MANAGER'
    AND p.is_active = TRUE
)
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- STORE_MANAGER 역할에 POS시스템 권한 추가
-- ========================================
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-store-manager-', m.menu_id, '-write'),
    m.menu_id,
    'ROLE',
    'STORE_MANAGER',
    'WRITE',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
AND m.menu_code LIKE 'POS_%'  -- POS시스템 메뉴만
AND NOT EXISTS (
    SELECT 1 FROM permissions p 
    WHERE p.menu_id = m.menu_id 
    AND p.target_type = 'ROLE' 
    AND p.target_id = 'STORE_MANAGER'
    AND p.is_active = TRUE
)
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- USER 역할에 POS 판매 기능만 READ 권한 추가
-- ========================================
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by, created_at, updated_at)
SELECT 
    CONCAT('perm-user-', m.menu_id, '-read'),
    m.menu_id,
    'ROLE',
    'USER',
    'READ',
    'system',
    NOW(),
    NOW()
FROM menus m 
WHERE m.is_active = TRUE
AND m.menu_code = 'POS_SALES'  -- POS 판매 기능만
AND NOT EXISTS (
    SELECT 1 FROM permissions p 
    WHERE p.menu_id = m.menu_id 
    AND p.target_type = 'ROLE' 
    AND p.target_id = 'USER'
    AND p.is_active = TRUE
)
ON DUPLICATE KEY UPDATE 
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- 권한 부여 검증 및 로그
-- ========================================

-- admin 사용자에게 부여된 권한 확인
SELECT 
    '권한 부여 완료' as status,
    COUNT(*) as total_permissions,
    COUNT(CASE WHEN target_type = 'USER' AND target_id = 'admin' THEN 1 END) as admin_user_permissions,
    COUNT(CASE WHEN target_type = 'ROLE' AND target_id = 'SUPER_ADMIN' THEN 1 END) as super_admin_role_permissions
FROM permissions 
WHERE is_active = TRUE
AND (
    (target_type = 'USER' AND target_id = 'admin') OR
    (target_type = 'ROLE' AND target_id = 'SUPER_ADMIN')
);

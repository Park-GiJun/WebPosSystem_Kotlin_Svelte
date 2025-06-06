-- ========================================
-- V5: 조직 관리 메뉴 및 권한 추가
-- ========================================

-- 사용자 테이블에 조직 정보 컬럼 추가
ALTER TABLE users 
ADD COLUMN organization_id VARCHAR(50) NULL AFTER user_status,
ADD COLUMN organization_type ENUM('SYSTEM', 'HEADQUARTERS', 'STORE') NULL AFTER organization_id;

-- 인덱스 추가
CREATE INDEX idx_users_organization ON users(organization_type, organization_id);

-- ========================================
-- 조직 관리 메뉴 추가
-- ========================================

-- 슈퍼어드민 하위에 조직 관리 메뉴 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES 
('menu-admin-organizations', 'ADMIN_ORGANIZATIONS', '조직 관리', '/admin/organizations', 'menu-admin', 2, 15, 'building', 'MENU', 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 조직 관리 하위 기능 메뉴들
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES 
('menu-admin-org-headquarters', 'ADMIN_ORG_HEADQUARTERS', '체인본부 관리', '/admin/organizations/headquarters', 'menu-admin-organizations', 3, 10, NULL, 'FUNCTION', 'system'),
('menu-admin-org-stores', 'ADMIN_ORG_STORES', '개인매장 관리', '/admin/organizations/stores', 'menu-admin-organizations', 3, 20, NULL, 'FUNCTION', 'system'),
('menu-admin-org-create', 'ADMIN_ORG_CREATE', '조직 생성', '/admin/organizations/create', 'menu-admin-organizations', 3, 30, NULL, 'FUNCTION', 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 본사 관리 전용 메뉴들 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES 
('menu-business-hq-stores', 'BUSINESS_HQ_STORES', '가맹점 관리', '/business/headquarters/stores', 'menu-business', 2, 15, 'building-storefront', 'MENU', 'system'),
('menu-business-hq-users', 'BUSINESS_HQ_USERS', '본사 직원 관리', '/business/headquarters/users', 'menu-business', 2, 25, 'users', 'MENU', 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 매장 관리 전용 메뉴들 추가  
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES 
('menu-pos-staff', 'POS_STAFF', '직원 관리', '/pos/staff', 'menu-pos', 2, 35, 'users', 'MENU', 'system'),
('menu-pos-system', 'POS_SYSTEM', 'POS 시스템 관리', '/pos/system', 'menu-pos', 2, 50, 'computer-desktop', 'MENU', 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ========================================
-- 권한 설정
-- ========================================

-- SUPER_ADMIN에게 조직 관리 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-super-admin-', menu_id, '-admin'),
    menu_id,
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system'
FROM menus 
WHERE menu_code LIKE 'ADMIN_ORG_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- HQ_MANAGER에게 본사 관리 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
VALUES 
('perm-hq-manager-hq-stores-admin', 'menu-business-hq-stores', 'ROLE', 'HQ_MANAGER', 'ADMIN', 'system'),
('perm-hq-manager-hq-users-admin', 'menu-business-hq-users', 'ROLE', 'HQ_MANAGER', 'ADMIN', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- STORE_MANAGER에게 매장 관리 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
VALUES 
('perm-store-manager-staff-admin', 'menu-pos-staff', 'ROLE', 'STORE_MANAGER', 'ADMIN', 'system'),
('perm-store-manager-system-admin', 'menu-pos-system', 'ROLE', 'STORE_MANAGER', 'ADMIN', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- ========================================
-- 기존 권한 업데이트
-- ========================================

-- 영업정보시스템 메뉴 권한 조정 (본사 관리자만 접근)
DELETE FROM permissions 
WHERE menu_id IN (
    SELECT menu_id FROM menus WHERE menu_code LIKE 'BUSINESS_%'
) AND target_type = 'ROLE' AND target_id NOT IN ('SUPER_ADMIN', 'SYSTEM_ADMIN', 'HQ_MANAGER');

-- POS 시스템 권한 조정 (매장 관련 역할만 접근)
DELETE FROM permissions 
WHERE menu_id IN (
    SELECT menu_id FROM menus WHERE menu_code LIKE 'POS_%'
) AND target_type = 'ROLE' AND target_id NOT IN ('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER');

-- ========================================
-- 조직별 권한 관리를 위한 뷰 업데이트
-- ========================================

-- 사용자별 메뉴 권한 뷰 업데이트 (조직 정보 포함)
DROP VIEW IF EXISTS user_menu_permissions;

CREATE OR REPLACE VIEW user_menu_permissions AS
SELECT DISTINCT
    u.id as user_id,
    u.username,
    u.organization_type,
    u.organization_id,
    m.menu_id,
    m.menu_code,
    m.menu_name,
    m.menu_path,
    m.parent_menu_id,
    m.menu_level,
    m.display_order,
    m.icon_name,
    m.menu_type,
    MAX(CASE 
        WHEN p.permission_type = 'ADMIN' THEN 4
        WHEN p.permission_type = 'DELETE' THEN 3
        WHEN p.permission_type = 'WRITE' THEN 2
        WHEN p.permission_type = 'READ' THEN 1
        ELSE 0
    END) as max_permission_level,
    CASE 
        WHEN MAX(CASE WHEN p.permission_type = 'ADMIN' THEN 1 ELSE 0 END) = 1 THEN 'ADMIN'
        WHEN MAX(CASE WHEN p.permission_type = 'DELETE' THEN 1 ELSE 0 END) = 1 THEN 'DELETE'
        WHEN MAX(CASE WHEN p.permission_type = 'WRITE' THEN 1 ELSE 0 END) = 1 THEN 'WRITE'
        WHEN MAX(CASE WHEN p.permission_type = 'READ' THEN 1 ELSE 0 END) = 1 THEN 'READ'
        ELSE NULL
    END as permission_type
FROM users u
CROSS JOIN menus m
LEFT JOIN permissions p ON (
    (p.target_type = 'USER' AND p.target_id = u.id) OR
    (p.target_type = 'ROLE' AND p.target_id IN (
        SELECT JSON_UNQUOTE(JSON_EXTRACT(u.roles, CONCAT('$[', idx.i, ']')))
        FROM (
            SELECT 0 as i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
        ) idx
        WHERE JSON_UNQUOTE(JSON_EXTRACT(u.roles, CONCAT('$[', idx.i, ']'))) IS NOT NULL
    )) OR
    (p.target_type = 'STORE' AND p.target_id = u.organization_id AND u.organization_type = 'STORE') OR
    (p.target_type = 'HEADQUARTERS' AND p.target_id = u.organization_id AND u.organization_type = 'HEADQUARTERS')
) AND p.menu_id = m.menu_id
  AND p.is_active = TRUE
  AND (p.expires_at IS NULL OR p.expires_at > NOW())
WHERE u.is_active = TRUE AND m.is_active = TRUE
GROUP BY u.id, u.username, u.organization_type, u.organization_id, m.menu_id, m.menu_code, m.menu_name, 
         m.menu_path, m.parent_menu_id, m.menu_level, m.display_order, m.icon_name, m.menu_type
HAVING max_permission_level > 0;

-- ========================================
-- 초기 시스템 사용자 업데이트
-- ========================================

-- 기존 SUPER_ADMIN 사용자를 시스템 타입으로 업데이트
UPDATE users 
SET organization_type = 'SYSTEM', 
    organization_id = NULL
WHERE JSON_CONTAINS(roles, '"SUPER_ADMIN"') = 1 
  AND organization_type IS NULL;

-- 기존 SYSTEM_ADMIN 사용자를 시스템 타입으로 업데이트  
UPDATE users 
SET organization_type = 'SYSTEM', 
    organization_id = NULL
WHERE JSON_CONTAINS(roles, '"SYSTEM_ADMIN"') = 1 
  AND organization_type IS NULL;

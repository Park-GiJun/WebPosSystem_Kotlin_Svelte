-- ========================================
-- V4: 메뉴 및 권한 관리 시스템
-- ========================================

-- ========================================
-- 1. 메뉴 관리 테이블
-- ========================================

-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menus
(
    menu_id        VARCHAR(36) PRIMARY KEY,
    menu_code      VARCHAR(50)  NOT NULL UNIQUE,
    menu_name      VARCHAR(100) NOT NULL,
    menu_path      VARCHAR(200) NOT NULL,
    parent_menu_id VARCHAR(36)  NULL,
    menu_level     INT                                      DEFAULT 1,
    display_order  INT                                      DEFAULT 0,
    icon_name      VARCHAR(50)  NULL,
    description    VARCHAR(200) NULL,
    menu_type      ENUM ('CATEGORY', 'MENU', 'FUNCTION')   DEFAULT 'MENU',

    -- Audit fields
    is_active      BOOLEAN                                  DEFAULT TRUE,
    created_at     TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(36),
    updated_at     TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(36),
    deleted_at     TIMESTAMP    NULL,
    deleted_by     VARCHAR(36),

    INDEX idx_menu_code (menu_code),
    INDEX idx_parent_menu (parent_menu_id),
    INDEX idx_menu_level (menu_level),
    INDEX idx_display_order (display_order),
    INDEX idx_menu_type (menu_type),
    INDEX idx_is_active (is_active),

    FOREIGN KEY (parent_menu_id) REFERENCES menus (menu_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 2. 권한 관리 테이블
-- ========================================

-- 권한 테이블
CREATE TABLE IF NOT EXISTS permissions
(
    permission_id   VARCHAR(36) PRIMARY KEY,
    menu_id         VARCHAR(36)                                     NOT NULL,
    target_type     ENUM ('USER', 'STORE', 'HEADQUARTERS', 'ROLE') NOT NULL,
    target_id       VARCHAR(50)                                     NOT NULL,
    permission_type ENUM ('READ', 'WRITE', 'DELETE', 'ADMIN')      NOT NULL,
    granted_at      TIMESTAMP                                       DEFAULT CURRENT_TIMESTAMP,
    granted_by      VARCHAR(36)                                     NOT NULL,
    expires_at      TIMESTAMP    NULL,
    is_active       BOOLEAN                                         DEFAULT TRUE,
    created_at      TIMESTAMP                                       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_permission (menu_id, target_type, target_id, permission_type),
    INDEX idx_menu_id (menu_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_granted_by (granted_by),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),

    FOREIGN KEY (menu_id) REFERENCES menus (menu_id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 3. 기본 메뉴 데이터 삽입
-- ========================================

-- 기본 메뉴 구조 생성
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES
    -- Level 1: 주요 카테고리
    ('menu-admin', 'ADMIN', '슈퍼어드민', '/admin', NULL, 1, 10, 'shield', 'CATEGORY', NULL),
    ('menu-business', 'BUSINESS', '영업정보시스템', '/business', NULL, 1, 20, 'building-office', 'CATEGORY', NULL),
    ('menu-pos', 'POS', 'POS시스템', '/pos', NULL, 1, 30, 'computer-desktop', 'CATEGORY', NULL),

    -- Level 2: 슈퍼어드민 하위 메뉴
    ('menu-admin-users', 'ADMIN_USERS', '사용자 관리', '/admin/users', 'menu-admin', 2, 10, 'users', 'MENU', NULL),
    ('menu-admin-permissions', 'ADMIN_PERMISSIONS', '권한 관리', '/admin/permissions', 'menu-admin', 2, 20, 'key', 'MENU', NULL),
    ('menu-admin-system', 'ADMIN_SYSTEM', '시스템 설정', '/admin/system', 'menu-admin', 2, 30, 'cog', 'MENU', NULL),
    ('menu-admin-audit', 'ADMIN_AUDIT', '감사 로그', '/admin/audit', 'menu-admin', 2, 40, 'document-text', 'MENU', NULL),

    -- Level 2: 영업정보시스템 하위 메뉴
    ('menu-business-hq', 'BUSINESS_HQ', '본사 관리', '/business/headquarters', 'menu-business', 2, 10, 'building-office-2', 'MENU', NULL),
    ('menu-business-stores', 'BUSINESS_STORES', '매장 관리', '/business/stores', 'menu-business', 2, 20, 'building-storefront', 'MENU', NULL),
    ('menu-business-reports', 'BUSINESS_REPORTS', '매출 분석', '/business/reports', 'menu-business', 2, 30, 'chart-bar', 'MENU', NULL),
    ('menu-business-inventory', 'BUSINESS_INVENTORY', '재고 관리', '/business/inventory', 'menu-business', 2, 40, 'cube', 'MENU', NULL),

    -- Level 2: POS시스템 하위 메뉴
    ('menu-pos-sales', 'POS_SALES', '판매', '/pos/sales', 'menu-pos', 2, 10, 'shopping-cart', 'MENU', NULL),
    ('menu-pos-products', 'POS_PRODUCTS', '상품 관리', '/pos/products', 'menu-pos', 2, 20, 'cube', 'MENU', NULL),
    ('menu-pos-customers', 'POS_CUSTOMERS', '고객 관리', '/pos/customers', 'menu-pos', 2, 30, 'user-group', 'MENU', NULL),
    ('menu-pos-settings', 'POS_SETTINGS', 'POS 설정', '/pos/settings', 'menu-pos', 2, 40, 'adjustments', 'MENU', NULL),

    -- Level 3: 세부 기능
    ('menu-admin-users-list', 'ADMIN_USERS_LIST', '사용자 목록', '/admin/users/list', 'menu-admin-users', 3, 10, NULL, 'FUNCTION', NULL),
    ('menu-admin-users-create', 'ADMIN_USERS_CREATE', '사용자 생성', '/admin/users/create', 'menu-admin-users', 3, 20, NULL, 'FUNCTION', NULL),
    ('menu-admin-users-roles', 'ADMIN_USERS_ROLES', '역할 관리', '/admin/users/roles', 'menu-admin-users', 3, 30, NULL, 'FUNCTION', NULL)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ========================================
-- 4. 기본 권한 설정
-- ========================================

-- SUPER_ADMIN 역할에 모든 메뉴 접근 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-super-admin-', menu_id, '-admin'),
    menu_id,
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system'
FROM menus 
WHERE is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- SYSTEM_ADMIN 역할에 시스템 관리 권한 부여 (슈퍼어드민 제외)
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-system-admin-', menu_id, '-admin'),
    menu_id,
    'ROLE',
    'SYSTEM_ADMIN',
    'ADMIN',
    'system'
FROM menus 
WHERE menu_code NOT LIKE 'ADMIN_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- HQ_MANAGER 역할에 영업정보시스템 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-hq-manager-', menu_id, '-write'),
    menu_id,
    'ROLE',
    'HQ_MANAGER',
    'WRITE',
    'system'
FROM menus 
WHERE menu_code LIKE 'BUSINESS_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- STORE_MANAGER 역할에 POS시스템 권한 부여
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-store-manager-', menu_id, '-write'),
    menu_id,
    'ROLE',
    'STORE_MANAGER',
    'WRITE',
    'system'
FROM menus 
WHERE menu_code LIKE 'POS_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- USER 역할에 기본 읽기 권한 부여 (POS 판매 기능만)
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
VALUES 
('perm-user-pos-sales-read', 'menu-pos-sales', 'ROLE', 'USER', 'READ', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- ========================================
-- 5. 권한 조회를 위한 뷰 생성
-- ========================================

-- 사용자별 메뉴 권한 뷰
CREATE OR REPLACE VIEW user_menu_permissions AS
SELECT DISTINCT
    u.id as user_id,
    u.username,
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
    (p.target_type = 'STORE' AND p.target_id IN (
        SELECT usp.store_id 
        FROM user_store_permissions usp 
        WHERE usp.user_id = u.id AND usp.is_active = TRUE
    ))
) AND p.menu_id = m.menu_id
  AND p.is_active = TRUE
  AND (p.expires_at IS NULL OR p.expires_at > NOW())
WHERE u.is_active = TRUE AND m.is_active = TRUE
GROUP BY u.id, u.username, m.menu_id, m.menu_code, m.menu_name, m.menu_path, 
         m.parent_menu_id, m.menu_level, m.display_order, m.icon_name, m.menu_type
HAVING max_permission_level > 0;

-- 메뉴 계층 구조 뷰
CREATE OR REPLACE VIEW menu_hierarchy AS
WITH RECURSIVE menu_tree AS (
    -- 루트 메뉴들
    SELECT 
        menu_id, menu_code, menu_name, menu_path,
        parent_menu_id, menu_level, display_order, icon_name, menu_type,
        menu_name as full_path,
        0 as depth
    FROM menus 
    WHERE parent_menu_id IS NULL AND is_active = TRUE
    
    UNION ALL
    
    -- 하위 메뉴들
    SELECT 
        m.menu_id, m.menu_code, m.menu_name, m.menu_path,
        m.parent_menu_id, m.menu_level, m.display_order, m.icon_name, m.menu_type,
        CONCAT(mt.full_path, ' > ', m.menu_name) as full_path,
        mt.depth + 1 as depth
    FROM menus m
    INNER JOIN menu_tree mt ON m.parent_menu_id = mt.menu_id
    WHERE m.is_active = TRUE
)
SELECT * FROM menu_tree
ORDER BY menu_level, display_order;

-- ========================================
-- 6. 권한 체크 함수 (저장 프로시저)
-- ========================================

DELIMITER //

CREATE OR REPLACE FUNCTION check_user_menu_permission(
    p_user_id VARCHAR(36),
    p_menu_code VARCHAR(50),
    p_required_permission ENUM('READ', 'WRITE', 'DELETE', 'ADMIN')
) RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE permission_level INT DEFAULT 0;
    DECLARE required_level INT;
    
    -- 요구되는 권한 레벨 설정
    SET required_level = CASE p_required_permission
        WHEN 'READ' THEN 1
        WHEN 'WRITE' THEN 2
        WHEN 'DELETE' THEN 3
        WHEN 'ADMIN' THEN 4
    END;
    
    -- 사용자의 해당 메뉴에 대한 최대 권한 레벨 조회
    SELECT COALESCE(MAX(max_permission_level), 0)
    INTO permission_level
    FROM user_menu_permissions
    WHERE user_id = p_user_id AND menu_code = p_menu_code;
    
    RETURN permission_level >= required_level;
END //

DELIMITER ;

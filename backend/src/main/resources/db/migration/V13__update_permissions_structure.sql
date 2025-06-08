-- V13__update_permissions_structure.sql
-- 권한 테이블 구조 업데이트

-- 기존 permissions 테이블을 백업하고 새로운 구조로 재생성
DROP TABLE IF EXISTS permissions_backup;
CREATE TABLE permissions_backup AS SELECT * FROM permissions;

-- permissions 테이블 구조 변경
DROP TABLE permissions;

CREATE TABLE permissions (
    id                     VARCHAR(36) PRIMARY KEY,
    permission_type        ENUM('READ', 'WRITE', 'DELETE', 'ADMIN') NOT NULL DEFAULT 'read',
    permission_target_type ENUM('USER', 'ROLE', 'ORGANIZATION') NOT NULL DEFAULT 'USER',
    menu_code              VARCHAR(50) NOT NULL,
    target_id              VARCHAR(50) NOT NULL,
    granted_by             VARCHAR(36) NULL,
    is_active              BOOLEAN DEFAULT TRUE,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_permission (permission_target_type, menu_code, target_id, permission_type),
    INDEX idx_menu_code (menu_code),
    INDEX idx_target (permission_target_type, target_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_granted_by (granted_by),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 기본 역할별 권한 재설정
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by)
VALUES 
-- ADMIN 역할 권한 (모든 메뉴)
('perm_admin_users_admin', 'ADMIN', 'ROLE', 'ADMIN_USERS', 'ADMIN', 'system'),
('perm_admin_permissions_admin', 'ADMIN', 'ROLE', 'ADMIN_PERMISSIONS', 'ADMIN', 'system'),
('perm_admin_orgs_admin', 'ADMIN', 'ROLE', 'ADMIN_ORGANIZATIONS', 'ADMIN', 'system'),
('perm_admin_system_admin', 'ADMIN', 'ROLE', 'ADMIN_SYSTEM', 'ADMIN', 'system'),

-- HEADQUARTERS_ADMIN 역할 권한
('perm_hq_admin_stores_write', 'WRITE', 'ROLE', 'BUSINESS_STORES', 'HEADQUARTERS_ADMIN', 'system'),
('perm_hq_admin_hq_write', 'WRITE', 'ROLE', 'BUSINESS_HQ', 'HEADQUARTERS_ADMIN', 'system'),
('perm_hq_admin_reports_read', 'READ', 'ROLE', 'BUSINESS_REPORTS', 'HEADQUARTERS_ADMIN', 'system'),
('perm_hq_admin_pos_read', 'READ', 'ROLE', 'BUSINESS_POS', 'HEADQUARTERS_ADMIN', 'system'),

-- STORE_ADMIN 역할 권한  
('perm_store_admin_pos_admin', 'ADMIN', 'ROLE', 'POS_SALES', 'STORE_ADMIN', 'system'),
('perm_store_admin_products_write', 'WRITE', 'ROLE', 'POS_PRODUCTS', 'STORE_ADMIN', 'system'),
('perm_store_admin_customers_write', 'WRITE', 'ROLE', 'POS_CUSTOMERS', 'STORE_ADMIN', 'system'),
('perm_store_admin_settings_write', 'WRITE', 'ROLE', 'POS_SETTINGS', 'STORE_ADMIN', 'system'),

-- USER 역할 권한 (기본 POS 사용)
('perm_user_pos_sales_read', 'READ', 'ROLE', 'POS_SALES', 'USER', 'system'),
('perm_user_pos_products_read', 'READ', 'ROLE', 'POS_PRODUCTS', 'USER', 'system');

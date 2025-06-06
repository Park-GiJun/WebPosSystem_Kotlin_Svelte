-- V1 DATABASE SCHEMA
-- User & Store Management System with Menu & Permission

-- 지역 코드 테이블
CREATE TABLE IF NOT EXISTS region_codes (
    region_code   VARCHAR(3) PRIMARY KEY,
    region_name   VARCHAR(50) NOT NULL,
    parent_region VARCHAR(3),
    display_order INT DEFAULT 0,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_region (parent_region),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id                    VARCHAR(36) PRIMARY KEY,
    username              VARCHAR(50) NOT NULL UNIQUE,
    email                 VARCHAR(320) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    roles                 JSON NOT NULL DEFAULT ('["USER"]'),
    user_status           ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED') DEFAULT 'ACTIVE',
    last_login_at         TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    locked_until          TIMESTAMP NULL,
    email_verified_at     TIMESTAMP NULL,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(36),
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(36),
    deleted_at            TIMESTAMP NULL,
    deleted_by            VARCHAR(36),
    version               BIGINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_status (user_status),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at),
    INDEX idx_email_verified (email_verified_at),
    INDEX idx_last_login (last_login_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 시스템 사용자는 애플리케이션에서 동적으로 생성하므로 여기서는 제거
-- INSERT INTO users 구문을 주석 처리함

-- User login history table
CREATE TABLE IF NOT EXISTS user_login_history (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        VARCHAR(36) NOT NULL,
    login_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(45),
    user_agent     TEXT,
    success        BOOLEAN NOT NULL,
    failure_reason VARCHAR(100),
    INDEX idx_user_id (user_id),
    INDEX idx_login_at (login_at),
    INDEX idx_success (success)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menus (
    menu_id        VARCHAR(36) PRIMARY KEY,
    menu_code      VARCHAR(50) NOT NULL UNIQUE,
    menu_name      VARCHAR(100) NOT NULL,
    menu_path      VARCHAR(200) NOT NULL,
    parent_menu_id VARCHAR(36) NULL,
    menu_level     INT DEFAULT 1,
    display_order  INT DEFAULT 0,
    icon_name      VARCHAR(50) NULL,
    description    VARCHAR(200) NULL,
    menu_type      ENUM ('CATEGORY', 'MENU', 'FUNCTION') DEFAULT 'MENU',
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(36),
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(36),
    deleted_at     TIMESTAMP NULL,
    deleted_by     VARCHAR(36),
    INDEX idx_menu_code (menu_code),
    INDEX idx_parent_menu (parent_menu_id),
    INDEX idx_menu_level (menu_level),
    INDEX idx_display_order (display_order),
    INDEX idx_menu_type (menu_type),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 권한 테이블
CREATE TABLE IF NOT EXISTS permissions (
    permission_id   VARCHAR(100) PRIMARY KEY,
    menu_id         VARCHAR(36) NOT NULL,
    target_type     ENUM ('USER', 'STORE', 'HEADQUARTERS', 'ROLE') NOT NULL,
    target_id       VARCHAR(50) NOT NULL,
    permission_type ENUM ('READ', 'WRITE', 'DELETE', 'ADMIN') NOT NULL,
    granted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by      VARCHAR(36) NULL,
    expires_at      TIMESTAMP NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_permission (menu_id, target_type, target_id, permission_type),
    INDEX idx_menu_id (menu_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_granted_by (granted_by),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 본사 정보 테이블
CREATE TABLE IF NOT EXISTS headquarters (
    hq_id                VARCHAR(10) PRIMARY KEY,
    hq_code              VARCHAR(5) NOT NULL UNIQUE,
    hq_name              VARCHAR(100) NOT NULL,
    business_license     VARCHAR(50),
    ceo_name             VARCHAR(50),
    headquarters_address VARCHAR(200),
    contact_phone        VARCHAR(20),
    website              VARCHAR(100),
    is_active            BOOLEAN DEFAULT TRUE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(36),
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(36),
    deleted_at           TIMESTAMP NULL,
    deleted_by           VARCHAR(36),
    version              BIGINT DEFAULT 0,
    INDEX idx_hq_code (hq_code),
    INDEX idx_hq_name (hq_name),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 매장 정보 테이블
CREATE TABLE IF NOT EXISTS stores (
    store_id         VARCHAR(20) PRIMARY KEY,
    store_name       VARCHAR(100) NOT NULL,
    store_type       ENUM ('CHAIN', 'INDIVIDUAL') NOT NULL,
    operation_type   ENUM ('DIRECT', 'FRANCHISE') NULL,
    hq_id            VARCHAR(10) NULL,
    region_code      VARCHAR(3) NOT NULL,
    store_number     VARCHAR(3) NOT NULL,
    business_license VARCHAR(50),
    owner_name       VARCHAR(50) NOT NULL,
    phone_number     VARCHAR(20),
    address          VARCHAR(200),
    postal_code      VARCHAR(10),
    opening_date     DATE,
    store_status     ENUM ('ACTIVE', 'INACTIVE', 'CLOSED') DEFAULT 'ACTIVE',
    manager_user_id  VARCHAR(36),
    is_active        BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(36),
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(36),
    deleted_at       TIMESTAMP NULL,
    deleted_by       VARCHAR(36),
    version          BIGINT DEFAULT 0,
    INDEX idx_store_name (store_name),
    INDEX idx_store_type (store_type),
    INDEX idx_operation_type (operation_type),
    INDEX idx_hq_region (hq_id, region_code),
    INDEX idx_store_status (store_status),
    INDEX idx_is_active (is_active),
    INDEX idx_manager (manager_user_id),
    INDEX idx_region_code (region_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- POS 시스템 테이블
CREATE TABLE IF NOT EXISTS pos_systems (
    pos_id                VARCHAR(20) PRIMARY KEY,
    store_id              VARCHAR(20) NOT NULL,
    pos_number            INT NOT NULL,
    pos_name              VARCHAR(50),
    pos_type              ENUM ('MAIN', 'SUB', 'MOBILE') DEFAULT 'MAIN',
    ip_address            VARCHAR(15),
    mac_address           VARCHAR(17),
    serial_number         VARCHAR(50),
    installed_date        DATE DEFAULT (CURRENT_DATE),
    last_maintenance_date DATE DEFAULT (CURRENT_DATE),
    pos_status            ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    is_active             BOOLEAN DEFAULT TRUE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(36),
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(36),
    deleted_at            TIMESTAMP NULL,
    deleted_by            VARCHAR(36),
    version               BIGINT DEFAULT 0,
    INDEX idx_store_pos (store_id, pos_number),
    INDEX idx_pos_type (pos_type),
    INDEX idx_pos_status (pos_status),
    INDEX idx_is_active (is_active),
    INDEX idx_ip_address (ip_address),
    INDEX idx_serial_number (serial_number)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 사용자-매장 관계 테이블
CREATE TABLE IF NOT EXISTS user_store_permissions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL,
    store_id        VARCHAR(20) NOT NULL,
    permission_type ENUM ('read', 'WRITE', 'ADMIN') NOT NULL,
    granted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by      VARCHAR(36),
    expires_at      TIMESTAMP NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_store_id (store_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 도메인 이벤트 테이블
CREATE TABLE IF NOT EXISTS domain_events (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id       VARCHAR(36) NOT NULL UNIQUE,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id   VARCHAR(50) NOT NULL,
    event_data     JSON NOT NULL,
    event_version  INT DEFAULT 1,
    occurred_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at   TIMESTAMP NULL,
    INDEX idx_event_type (event_type),
    INDEX idx_aggregate (aggregate_type, aggregate_id),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_processed_at (processed_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 감사 로그 테이블
CREATE TABLE IF NOT EXISTS audit_logs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name     VARCHAR(50) NOT NULL,
    record_id      VARCHAR(50) NOT NULL,
    action         ENUM ('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values     JSON,
    new_values     JSON,
    changed_fields JSON,
    user_id        VARCHAR(36),
    ip_address     VARCHAR(45),
    user_agent     TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_action (action),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 기본 지역 코드 데이터
INSERT INTO region_codes (region_code, region_name, parent_region, display_order)
VALUES ('001', '서울특별시', NULL, 1),
       ('002', '부산광역시', NULL, 2),
       ('003', '대구광역시', NULL, 3),
       ('004', '인천광역시', NULL, 4),
       ('005', '광주광역시', NULL, 5),
       ('006', '대전광역시', NULL, 6),
       ('007', '울산광역시', NULL, 7),
       ('008', '세종특별자치시', NULL, 8),
       ('009', '경기도', NULL, 9),
       ('010', '강원도', NULL, 10),
       ('011', '충청북도', NULL, 11),
       ('012', '충청남도', NULL, 12),
       ('013', '전라북도', NULL, 13),
       ('014', '전라남도', NULL, 14),
       ('015', '경상북도', NULL, 15),
       ('016', '경상남도', NULL, 16),
       ('017', '제주특별자치도', NULL, 17)
ON DUPLICATE KEY UPDATE region_name = VALUES(region_name);

-- 기본 메뉴 구조 생성
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES
    ('menu-admin', 'ADMIN', '슈퍼어드민', '/admin', NULL, 1, 10, 'shield', 'CATEGORY', NULL),
    ('menu-business', 'BUSINESS', '영업정보시스템', '/business', NULL, 1, 20, 'building-office', 'CATEGORY', NULL),
    ('menu-pos', 'POS', 'POS시스템', '/pos', NULL, 1, 30, 'computer-desktop', 'CATEGORY', NULL),
    ('menu-admin-users', 'ADMIN_USERS', '사용자 관리', '/admin/users', 'menu-admin', 2, 10, 'users', 'MENU', NULL),
    ('menu-admin-permissions', 'ADMIN_PERMISSIONS', '권한 관리', '/admin/permissions', 'menu-admin', 2, 20, 'key', 'MENU', NULL),
    ('menu-admin-system', 'ADMIN_SYSTEM', '시스템 설정', '/admin/system', 'menu-admin', 2, 30, 'cog', 'MENU', NULL),
    ('menu-admin-audit', 'ADMIN_AUDIT', '감사 로그', '/admin/audit', 'menu-admin', 2, 40, 'document-text', 'MENU', NULL),
    ('menu-business-hq', 'BUSINESS_HQ', '본사 관리', '/business/headquarters', 'menu-business', 2, 10, 'building-office-2', 'MENU', NULL),
    ('menu-business-stores', 'BUSINESS_STORES', '매장 관리', '/business/stores', 'menu-business', 2, 20, 'building-storefront', 'MENU', NULL),
    ('menu-business-reports', 'BUSINESS_REPORTS', '매출 분석', '/business/reports', 'menu-business', 2, 30, 'chart-bar', 'MENU', NULL),
    ('menu-business-inventory', 'BUSINESS_INVENTORY', '재고 관리', '/business/inventory', 'menu-business', 2, 40, 'cube', 'MENU', NULL),
    ('menu-pos-sales', 'POS_SALES', '판매', '/pos/sales', 'menu-pos', 2, 10, 'shopping-cart', 'MENU', NULL),
    ('menu-pos-products', 'POS_PRODUCTS', '상품 관리', '/pos/products', 'menu-pos', 2, 20, 'cube', 'MENU', NULL),
    ('menu-pos-customers', 'POS_CUSTOMERS', '고객 관리', '/pos/customers', 'menu-pos', 2, 30, 'user-group', 'MENU', NULL),
    ('menu-pos-settings', 'POS_SETTINGS', 'POS 설정', '/pos/settings', 'menu-pos', 2, 40, 'adjustments', 'MENU', NULL)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 기본 권한 설정 (시스템 권한)
INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-sa-', RIGHT(menu_id, 8)),
    menu_id,
    'ROLE',
    'SUPER_ADMIN',
    'ADMIN',
    'system'
FROM menus 
WHERE is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-sysadm-', RIGHT(menu_id, 8)),
    menu_id,
    'ROLE',
    'SYSTEM_ADMIN',
    'ADMIN',
    'system'
FROM menus 
WHERE menu_code NOT LIKE 'ADMIN_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-hqmgr-', RIGHT(menu_id, 8)),
    menu_id,
    'ROLE',
    'HQ_MANAGER',
    'WRITE',
    'system'
FROM menus 
WHERE menu_code LIKE 'BUSINESS_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
SELECT 
    CONCAT('perm-storemgr-', RIGHT(menu_id, 8)),
    menu_id,
    'ROLE',
    'STORE_MANAGER',
    'WRITE',
    'system'
FROM menus 
WHERE menu_code LIKE 'POS_%' AND is_active = TRUE
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

INSERT INTO permissions (permission_id, menu_id, target_type, target_id, permission_type, granted_by)
VALUES ('perm-user-pos-sales-read', 'menu-pos-sales', 'ROLE', 'USER', 'READ', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- ========================================
-- V1: 통합 초기 스키마 (현재 백엔드 소스 기준)
-- WebPos 시스템 완전한 초기 스키마
-- ========================================

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

-- 사용자 테이블 (현재 백엔드 UserEntity 기준)
CREATE TABLE IF NOT EXISTS users (
    id                    VARCHAR(36) PRIMARY KEY,
    username              VARCHAR(50) NOT NULL UNIQUE,
    email                 VARCHAR(320) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    roles                 JSON NOT NULL DEFAULT ('["USER"]'),
    user_status           ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED') DEFAULT 'ACTIVE',
    organization_id       VARCHAR(50) NULL COMMENT '소속 조직 ID (본사 또는 매장)',
    organization_type     VARCHAR(20) NULL COMMENT '조직 유형 (HEADQUARTERS, STORE, SYSTEM)',
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
    INDEX idx_last_login (last_login_at),
    INDEX idx_organization (organization_id, organization_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 사용자 로그인 히스토리 테이블
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

-- 권한 테이블 (V13 최신 구조 기준)
CREATE TABLE IF NOT EXISTS permissions (
    id                     VARCHAR(36) PRIMARY KEY,
    permission_type        ENUM('read', 'write', 'delete', 'admin') NOT NULL DEFAULT 'read',
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

-- 본사 정보 테이블
CREATE TABLE IF NOT EXISTS headquarters (
    hq_id                VARCHAR(10) PRIMARY KEY,
    hq_code              VARCHAR(5) NOT NULL UNIQUE,
    hq_name              VARCHAR(100) NOT NULL,
    business_license     VARCHAR(50),
    ceo_name             VARCHAR(50),
    headquarters_address VARCHAR(200),
    contact_phone        VARCHAR(20),
    website              VARCHAR(200),
    registration_date    DATE,
    email                VARCHAR(320),
    fax_number           VARCHAR(20),
    description          TEXT,
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
    store_id            VARCHAR(20) PRIMARY KEY,
    store_code          VARCHAR(10) NOT NULL UNIQUE,
    store_name          VARCHAR(100) NOT NULL,
    hq_id               VARCHAR(10),
    store_address       VARCHAR(200),
    contact_phone       VARCHAR(20),
    store_manager_id    VARCHAR(36),
    registration_date   DATE,
    business_hours      VARCHAR(100),
    store_type          VARCHAR(50),
    store_status        ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(36),
    deleted_at          TIMESTAMP NULL,
    deleted_by          VARCHAR(36),
    version             BIGINT DEFAULT 0,
    INDEX idx_store_code (store_code),
    INDEX idx_store_name (store_name),
    INDEX idx_hq_id (hq_id),
    INDEX idx_store_manager (store_manager_id),
    INDEX idx_store_status (store_status),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (hq_id) REFERENCES headquarters(hq_id),
    FOREIGN KEY (store_manager_id) REFERENCES users(id)
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
    action_type    ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values     JSON,
    new_values     JSON,
    changed_fields JSON,
    user_id        VARCHAR(36),
    user_name      VARCHAR(50),
    ip_address     VARCHAR(45),
    user_agent     TEXT,
    description    VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_action_type (action_type),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 기본 데이터 삽입
-- ========================================

-- 기본 지역 코드 데이터
INSERT INTO region_codes (region_code, region_name, parent_region, display_order) VALUES
('000', '전국', NULL, 0),
('001', '서울특별시', '000', 1),
('002', '부산광역시', '000', 2),
('003', '대구광역시', '000', 3),
('004', '인천광역시', '000', 4),
('005', '광주광역시', '000', 5),
('006', '대전광역시', '000', 6),
('007', '울산광역시', '000', 7),
('008', '세종특별자치시', '000', 8),
('031', '경기도', '000', 9),
('033', '강원도', '000', 10),
('043', '충청북도', '000', 11),
('041', '충청남도', '000', 12),
('063', '전라북도', '000', 13),
('061', '전라남도', '000', 14),
('054', '경상북도', '000', 15),
('055', '경상남도', '000', 16),
('064', '제주특별자치도', '000', 17)
ON DUPLICATE KEY UPDATE region_name = VALUES(region_name);

-- 기본 메뉴 데이터
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, description) VALUES
-- 최상위 메뉴
('menu-admin', 'ADMIN', '시스템 관리', '/admin', NULL, 1, 10, 'settings', 'CATEGORY', '시스템 전체 관리'),
('menu-business', 'BUSINESS', '영업정보시스템', '/business', NULL, 1, 20, 'briefcase', 'CATEGORY', '본사 및 매장 관리'),
('menu-pos', 'POS', 'POS 시스템', '/pos', NULL, 1, 30, 'credit-card', 'CATEGORY', '매장 POS 운영'),

-- 시스템 관리 하위 메뉴
('menu-admin-users', 'ADMIN_USERS', '사용자 관리', '/admin/users', 'menu-admin', 2, 10, 'users', 'MENU', '시스템 사용자 관리'),
('menu-admin-permissions', 'ADMIN_PERMISSIONS', '권한 관리', '/admin/permissions', 'menu-admin', 2, 20, 'key', 'MENU', '사용자 권한 설정'),
('menu-admin-organizations', 'ADMIN_ORGANIZATIONS', '조직 관리', '/admin/organizations', 'menu-admin', 2, 30, 'building', 'MENU', '본사/매장 조직 관리'),
('menu-admin-system', 'ADMIN_SYSTEM', '시스템 설정', '/admin/system', 'menu-admin', 2, 40, 'cog', 'MENU', '시스템 환경 설정'),
('menu-admin-audit', 'ADMIN_AUDIT', '감사 로그', '/admin/audit', 'menu-admin', 2, 50, 'file-text', 'MENU', '시스템 감사 로그 조회'),

-- 영업정보시스템 하위 메뉴
('menu-business-hq', 'BUSINESS_HQ', '본사 관리', '/business/headquarters', 'menu-business', 2, 10, 'home', 'MENU', '본사 정보 관리'),
('menu-business-stores', 'BUSINESS_STORES', '매장 관리', '/business/stores', 'menu-business', 2, 20, 'shop', 'MENU', '매장 정보 관리'),
('menu-business-reports', 'BUSINESS_REPORTS', '통계/보고서', '/business/reports', 'menu-business', 2, 30, 'bar-chart', 'MENU', '매출 및 운영 통계'),
('menu-business-pos', 'BUSINESS_POS', 'POS 관리', '/business/pos', 'menu-business', 2, 40, 'monitor', 'MENU', '매장 POS 기기 관리'),

-- POS 시스템 하위 메뉴
('menu-pos-sales', 'POS_SALES', '판매', '/pos/sales', 'menu-pos', 2, 10, 'shopping-cart', 'MENU', '상품 판매 처리'),
('menu-pos-products', 'POS_PRODUCTS', '상품 관리', '/pos/products', 'menu-pos', 2, 20, 'package', 'MENU', '상품 정보 관리'),
('menu-pos-customers', 'POS_CUSTOMERS', '고객 관리', '/pos/customers', 'menu-pos', 2, 30, 'user', 'MENU', '고객 정보 관리'),
('menu-pos-settings', 'POS_SETTINGS', 'POS 설정', '/pos/settings', 'menu-pos', 2, 40, 'settings', 'MENU', 'POS 기기 설정')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 기본 관리자 사용자 생성
INSERT INTO users (id, username, email, password_hash, roles, user_status, organization_type, email_verified_at, is_active, created_at, updated_at, version)
VALUES (
    'admin-user-id',
    'admin',
    'admin@webpos.com',
    '$2a$10$RUDKfsvPLlm75vp5DVccCej2esBU4iAYxGO7Pcj3nCwSjIERLmI32',  -- password123
    '["SUPER_ADMIN", "SYSTEM_ADMIN"]',
    'ACTIVE',
    'SYSTEM',
    NOW(),
    true,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 기본 권한 설정
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by) VALUES
-- SUPER_ADMIN 권한 (모든 관리 메뉴에 ADMIN 권한)
('perm-super-admin-admin', 'admin', 'ROLE', 'ADMIN', 'SUPER_ADMIN', 'system'),
('perm-super-admin-admin-users', 'admin', 'ROLE', 'ADMIN_USERS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-admin-permissions', 'admin', 'ROLE', 'ADMIN_PERMISSIONS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-admin-organizations', 'admin', 'ROLE', 'ADMIN_ORGANIZATIONS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-admin-system', 'admin', 'ROLE', 'ADMIN_SYSTEM', 'SUPER_ADMIN', 'system'),
('perm-super-admin-admin-audit', 'admin', 'ROLE', 'ADMIN_AUDIT', 'SUPER_ADMIN', 'system'),
('perm-super-admin-business', 'admin', 'ROLE', 'BUSINESS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-business-hq', 'admin', 'ROLE', 'BUSINESS_HQ', 'SUPER_ADMIN', 'system'),
('perm-super-admin-business-stores', 'admin', 'ROLE', 'BUSINESS_STORES', 'SUPER_ADMIN', 'system'),
('perm-super-admin-business-reports', 'admin', 'ROLE', 'BUSINESS_REPORTS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-business-pos', 'admin', 'ROLE', 'BUSINESS_POS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-pos', 'admin', 'ROLE', 'POS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-pos-sales', 'admin', 'ROLE', 'POS_SALES', 'SUPER_ADMIN', 'system'),
('perm-super-admin-pos-products', 'admin', 'ROLE', 'POS_PRODUCTS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-pos-customers', 'admin', 'ROLE', 'POS_CUSTOMERS', 'SUPER_ADMIN', 'system'),
('perm-super-admin-pos-settings', 'admin', 'ROLE', 'POS_SETTINGS', 'SUPER_ADMIN', 'system'),

-- SYSTEM_ADMIN 권한 (관리 메뉴 제외한 모든 메뉴)
('perm-system-admin-business', 'admin', 'ROLE', 'BUSINESS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-business-hq', 'admin', 'ROLE', 'BUSINESS_HQ', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-business-stores', 'admin', 'ROLE', 'BUSINESS_STORES', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-business-reports', 'admin', 'ROLE', 'BUSINESS_REPORTS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-business-pos', 'admin', 'ROLE', 'BUSINESS_POS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-pos', 'admin', 'ROLE', 'POS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-pos-sales', 'admin', 'ROLE', 'POS_SALES', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-pos-products', 'admin', 'ROLE', 'POS_PRODUCTS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-pos-customers', 'admin', 'ROLE', 'POS_CUSTOMERS', 'SYSTEM_ADMIN', 'system'),
('perm-system-admin-pos-settings', 'admin', 'ROLE', 'POS_SETTINGS', 'SYSTEM_ADMIN', 'system'),

-- HEADQUARTERS_ADMIN 권한
('perm-hq-admin-business-hq', 'write', 'ROLE', 'BUSINESS_HQ', 'HEADQUARTERS_ADMIN', 'system'),
('perm-hq-admin-business-stores', 'write', 'ROLE', 'BUSINESS_STORES', 'HEADQUARTERS_ADMIN', 'system'),
('perm-hq-admin-business-reports', 'read', 'ROLE', 'BUSINESS_REPORTS', 'HEADQUARTERS_ADMIN', 'system'),
('perm-hq-admin-business-pos', 'read', 'ROLE', 'BUSINESS_POS', 'HEADQUARTERS_ADMIN', 'system'),

-- HQ_MANAGER 권한
('perm-hq-mgr-business-hq', 'write', 'ROLE', 'BUSINESS_HQ', 'HQ_MANAGER', 'system'),
('perm-hq-mgr-business-stores', 'write', 'ROLE', 'BUSINESS_STORES', 'HQ_MANAGER', 'system'),
('perm-hq-mgr-business-reports', 'read', 'ROLE', 'BUSINESS_REPORTS', 'HQ_MANAGER', 'system'),
('perm-hq-mgr-business-pos', 'read', 'ROLE', 'BUSINESS_POS', 'HQ_MANAGER', 'system'),

-- STORE_ADMIN 권한
('perm-store-admin-pos-sales', 'admin', 'ROLE', 'POS_SALES', 'STORE_ADMIN', 'system'),
('perm-store-admin-pos-products', 'write', 'ROLE', 'POS_PRODUCTS', 'STORE_ADMIN', 'system'),
('perm-store-admin-pos-customers', 'write', 'ROLE', 'POS_CUSTOMERS', 'STORE_ADMIN', 'system'),
('perm-store-admin-pos-settings', 'write', 'ROLE', 'POS_SETTINGS', 'STORE_ADMIN', 'system'),

-- STORE_MANAGER 권한
('perm-store-mgr-pos-sales', 'write', 'ROLE', 'POS_SALES', 'STORE_MANAGER', 'system'),
('perm-store-mgr-pos-products', 'write', 'ROLE', 'POS_PRODUCTS', 'STORE_MANAGER', 'system'),
('perm-store-mgr-pos-customers', 'write', 'ROLE', 'POS_CUSTOMERS', 'STORE_MANAGER', 'system'),
('perm-store-mgr-pos-settings', 'read', 'ROLE', 'POS_SETTINGS', 'STORE_MANAGER', 'system'),

-- USER 권한 (기본 POS 사용)
('perm-user-pos-sales', 'read', 'ROLE', 'POS_SALES', 'USER', 'system'),
('perm-user-pos-products', 'read', 'ROLE', 'POS_PRODUCTS', 'USER', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- 기본 본사 데이터 (테스트용)
INSERT INTO headquarters (hq_id, hq_code, hq_name, business_license, ceo_name, headquarters_address, contact_phone, email, is_active, created_by) VALUES
('HQSTT', 'STT', '스타트업 테스트 본사', '123-45-67890', '김대표', '서울시 강남구 테헤란로 123', '02-1234-5678', 'ceo@startup.com', TRUE, 'admin-user-id'),
('HQTET', 'TET', '테스트 본사', '987-65-43210', '이사장', '서울시 서초구 서초대로 456', '02-9876-5432', 'ceo@test.com', TRUE, 'admin-user-id')
ON DUPLICATE KEY UPDATE hq_name = VALUES(hq_name);

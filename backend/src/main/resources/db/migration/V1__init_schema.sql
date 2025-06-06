-- ========================================
-- V2 DATABASE SCHEMA
-- User & Store Management System
-- ========================================

-- ========================================
-- 1. REGION & LOCATION TABLES
-- ========================================

-- 지역 코드 테이블
CREATE TABLE IF NOT EXISTS region_codes
(
    region_code   VARCHAR(3) PRIMARY KEY,
    region_name   VARCHAR(50) NOT NULL,
    parent_region VARCHAR(3),
    display_order INT       DEFAULT 0,
    is_active     BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_parent_region (parent_region),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 2. USER MANAGEMENT TABLES
-- ========================================

-- Users table (V2 - Enhanced)
CREATE TABLE IF NOT EXISTS users
(
    id                    VARCHAR(36) PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL UNIQUE,
    email                 VARCHAR(320) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    roles                 JSON         NOT NULL                                                      DEFAULT ('["USER"]'),
    user_status           ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED') DEFAULT 'ACTIVE',

    -- Security & Login tracking
    last_login_at         TIMESTAMP    NULL,
    failed_login_attempts INT                                                                        DEFAULT 0,
    locked_until          TIMESTAMP    NULL,
    email_verified_at     TIMESTAMP    NULL,

    -- Audit fields
    is_active             BOOLEAN      NOT NULL                                                      DEFAULT TRUE,
    created_at            TIMESTAMP    NOT NULL                                                      DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(36),
    updated_at            TIMESTAMP    NOT NULL                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(36),
    deleted_at            TIMESTAMP    NULL,
    deleted_by            VARCHAR(36),

    -- Version for optimistic locking
    version               BIGINT                                                                     DEFAULT 0,

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_status (user_status),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at),
    INDEX idx_email_verified (email_verified_at),
    INDEX idx_last_login (last_login_at),

    -- Foreign key constraints
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- User login history table
CREATE TABLE IF NOT EXISTS user_login_history
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        VARCHAR(36) NOT NULL,
    login_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(45),
    user_agent     TEXT,
    success        BOOLEAN     NOT NULL,
    failure_reason VARCHAR(100),

    INDEX idx_user_id (user_id),
    INDEX idx_login_at (login_at),
    INDEX idx_success (success),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 3. HEADQUARTERS MANAGEMENT
-- ========================================

-- 본사 정보 테이블
CREATE TABLE IF NOT EXISTS headquarters
(
    hq_id                VARCHAR(10) PRIMARY KEY,
    hq_code              VARCHAR(5)   NOT NULL UNIQUE,
    hq_name              VARCHAR(100) NOT NULL,
    business_license     VARCHAR(50),
    ceo_name             VARCHAR(50),
    headquarters_address VARCHAR(200),
    contact_phone        VARCHAR(20),
    website              VARCHAR(100),

    -- Audit fields
    is_active            BOOLEAN   DEFAULT TRUE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(36),
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(36),
    deleted_at           TIMESTAMP    NULL,
    deleted_by           VARCHAR(36),
    version              BIGINT    DEFAULT 0,

    INDEX idx_hq_code (hq_code),
    INDEX idx_hq_name (hq_name),
    INDEX idx_is_active (is_active),

    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 4. STORE MANAGEMENT
-- ========================================

-- 매장 정보 테이블
CREATE TABLE IF NOT EXISTS stores
(
    store_id         VARCHAR(20) PRIMARY KEY,
    store_name       VARCHAR(100)                 NOT NULL,
    store_type       ENUM ('CHAIN', 'INDIVIDUAL') NOT NULL,
    operation_type   ENUM ('DIRECT', 'FRANCHISE') NULL,
    hq_id            VARCHAR(10)                  NULL,
    region_code      VARCHAR(3)                   NOT NULL,
    store_number     VARCHAR(3)                   NOT NULL,
    business_license VARCHAR(50),
    owner_name       VARCHAR(50)                  NOT NULL,
    phone_number     VARCHAR(20),
    address          VARCHAR(200),
    postal_code      VARCHAR(10),
    opening_date     DATE,
    store_status     ENUM ('ACTIVE', 'INACTIVE', 'CLOSED') DEFAULT 'ACTIVE',

    -- Manager assignment
    manager_user_id  VARCHAR(36),

    -- Audit fields
    is_active        BOOLEAN                               DEFAULT TRUE,
    created_at       TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(36),
    updated_at       TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(36),
    deleted_at       TIMESTAMP                    NULL,
    deleted_by       VARCHAR(36),
    version          BIGINT                                DEFAULT 0,

    INDEX idx_store_name (store_name),
    INDEX idx_store_type (store_type),
    INDEX idx_operation_type (operation_type),
    INDEX idx_hq_region (hq_id, region_code),
    INDEX idx_store_status (store_status),
    INDEX idx_is_active (is_active),
    INDEX idx_manager (manager_user_id),
    INDEX idx_region_code (region_code),

    UNIQUE KEY unique_store_per_region (region_code, store_number, hq_id),

    FOREIGN KEY (hq_id) REFERENCES headquarters (hq_id) ON DELETE SET NULL,
    FOREIGN KEY (region_code) REFERENCES region_codes (region_code),
    FOREIGN KEY (manager_user_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 5. POS SYSTEM MANAGEMENT
-- ========================================

-- POS 시스템 테이블
CREATE TABLE IF NOT EXISTS pos_systems
(
    pos_id                VARCHAR(20) PRIMARY KEY,
    store_id              VARCHAR(20) NOT NULL,
    pos_number            INT         NOT NULL,
    pos_name              VARCHAR(50),
    pos_type              ENUM ('MAIN', 'SUB', 'MOBILE')             DEFAULT 'MAIN',
    ip_address            VARCHAR(15),
    mac_address           VARCHAR(17),
    serial_number         VARCHAR(50),
    installed_date        DATE                                       DEFAULT (CURRENT_DATE),
    last_maintenance_date DATE                                       DEFAULT (CURRENT_DATE),
    pos_status            ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',

    -- Audit fields
    is_active             BOOLEAN                                    DEFAULT TRUE,
    created_at            TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(36),
    updated_at            TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(36),
    deleted_at            TIMESTAMP   NULL,
    deleted_by            VARCHAR(36),
    version               BIGINT                                     DEFAULT 0,

    INDEX idx_store_pos (store_id, pos_number),
    INDEX idx_pos_type (pos_type),
    INDEX idx_pos_status (pos_status),
    INDEX idx_is_active (is_active),
    INDEX idx_ip_address (ip_address),
    INDEX idx_serial_number (serial_number),

    UNIQUE KEY unique_pos_per_store (store_id, pos_number),
    UNIQUE KEY unique_serial_number (serial_number),

    FOREIGN KEY (store_id) REFERENCES stores (store_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 6. USER-STORE RELATIONSHIPS
-- ========================================

-- 사용자-매장 관계 테이블 (권한 관리)
CREATE TABLE IF NOT EXISTS user_store_permissions
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         VARCHAR(36)                     NOT NULL,
    store_id        VARCHAR(20)                     NOT NULL,
    permission_type ENUM ('READ', 'write', 'admin') NOT NULL,
    granted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by      VARCHAR(36),
    expires_at      TIMESTAMP                       NULL,

    -- Audit fields
    is_active       BOOLEAN   DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_store_permission (user_id, store_id, permission_type),
    INDEX idx_user_id (user_id),
    INDEX idx_store_id (store_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores (store_id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 7. AUDIT & EVENT TABLES
-- ========================================

-- 도메인 이벤트 테이블
CREATE TABLE IF NOT EXISTS domain_events
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id       VARCHAR(36)  NOT NULL UNIQUE,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   VARCHAR(50)  NOT NULL,
    event_data     JSON         NOT NULL,
    event_version  INT       DEFAULT 1,
    occurred_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at   TIMESTAMP    NULL,

    INDEX idx_event_type (event_type),
    INDEX idx_aggregate (aggregate_type, aggregate_id),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_processed_at (processed_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 감사 로그 테이블
CREATE TABLE IF NOT EXISTS audit_logs
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name     VARCHAR(50)                         NOT NULL,
    record_id      VARCHAR(50)                         NOT NULL,
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
    INDEX idx_created_at (created_at),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ========================================
-- 8. INITIAL DATA
-- ========================================

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

-- ========================================
-- 9. MIGRATION SCRIPTS (V1 -> V2)
-- ========================================

-- V1 users 테이블에서 V2로 데이터 마이그레이션을 위한 스크립트
-- 주의: 실제 운영환경에서는 단계적으로 진행해야 함

/*
-- Step 1: V1 테이블 백업
CREATE TABLE users_v1_backup AS SELECT * FROM users;

-- Step 2: 새 컬럼 추가 (NULL 허용으로 시작)
ALTER TABLE users
ADD COLUMN user_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED') DEFAULT 'ACTIVE',
ADD COLUMN last_login_at TIMESTAMP NULL,
ADD COLUMN failed_login_attempts INT DEFAULT 0,
ADD COLUMN locked_until TIMESTAMP NULL,
ADD COLUMN email_verified_at TIMESTAMP NULL,
ADD COLUMN created_by VARCHAR(36),
ADD COLUMN updated_by VARCHAR(36),
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(36);

-- Step 3: 기존 활성 사용자들을 이메일 인증 완료로 설정
UPDATE users
SET email_verified_at = created_at
WHERE is_active = TRUE;

-- Step 4: 비활성 사용자들 상태 업데이트
UPDATE users
SET user_status = 'INACTIVE'
WHERE is_active = FALSE;

-- Step 5: 이메일 컬럼 크기 확장
ALTER TABLE users MODIFY COLUMN email VARCHAR(320) NOT NULL;

-- Step 6: 새 인덱스 추가
CREATE INDEX idx_user_status ON users(user_status);
CREATE INDEX idx_email_verified ON users(email_verified_at);
CREATE INDEX idx_last_login ON users(last_login_at);
*/

-- ========================================
-- 10. PERFORMANCE OPTIMIZATION
-- ========================================

-- 파티셔닝을 위한 테이블 (대용량 로그 데이터)
-- CREATE TABLE user_login_history_partitioned (
--     LIKE user_login_history
-- ) PARTITION BY RANGE (YEAR(login_at)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- ========================================
-- 11. VIEWS FOR COMMON QUERIES
-- ========================================

-- 활성 매장 정보 뷰
CREATE OR REPLACE VIEW active_stores AS
SELECT s.store_id,
       s.store_name,
       s.store_type,
       s.operation_type,
       h.hq_name,
       h.hq_code,
       s.region_code,
       rc.region_name,
       s.owner_name,
       s.phone_number,
       s.address,
       s.store_status,
       u.username as manager_username,
       s.created_at
FROM stores s
         LEFT JOIN headquarters h ON s.hq_id = h.hq_id
         LEFT JOIN region_codes rc ON s.region_code = rc.region_code
         LEFT JOIN users u ON s.manager_user_id = u.id
WHERE s.is_active = TRUE
  AND s.store_status != 'CLOSED';

-- 사용자 권한 정보 뷰
CREATE OR REPLACE VIEW user_permissions AS
SELECT u.id as user_id,
       u.username,
       u.email,
       u.roles,
       u.user_status,
       usp.store_id,
       s.store_name,
       usp.permission_type,
       usp.expires_at
FROM users u
         LEFT JOIN user_store_permissions usp ON u.id = usp.user_id AND usp.is_active = TRUE
         LEFT JOIN stores s ON usp.store_id = s.store_id
WHERE u.is_active = TRUE;

-- POS 시스템 현황 뷰
CREATE OR REPLACE VIEW pos_system_status AS
SELECT p.pos_id,
       p.store_id,
       s.store_name,
       p.pos_number,
       p.pos_name,
       p.pos_type,
       p.pos_status,
       p.last_maintenance_date,
       DATEDIFF(CURRENT_DATE, p.last_maintenance_date) as days_since_maintenance
FROM pos_systems p
         JOIN stores s ON p.store_id = s.store_id
WHERE p.is_active = TRUE
  AND s.is_active = TRUE;
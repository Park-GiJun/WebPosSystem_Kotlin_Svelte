-- ========================================
-- V8: 자동 사용자 생성 트리거 추가
-- ========================================

-- 사용자 테이블에 조직 정보 컬럼이 없는 경우에만 추가
-- (V5에서 이미 추가되었으므로 생략)

-- 조직 관련 인덱스 추가 (이미 존재하는 경우 무시)
-- MySQL에서는 IF NOT EXISTS를 지원하지 않으므로 다른 방법 사용

-- 기존 인덱스가 있으면 무시하고 없으면 생성
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
               WHERE table_schema = DATABASE() 
               AND table_name = 'users' 
               AND index_name = 'idx_users_organization_id') = 0,
              'CREATE INDEX idx_users_organization_id ON users(organization_id)',
              'SELECT ''Index idx_users_organization_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
               WHERE table_schema = DATABASE() 
               AND table_name = 'users' 
               AND index_name = 'idx_users_organization_type') = 0,
              'CREATE INDEX idx_users_organization_type ON users(organization_type)',
              'SELECT ''Index idx_users_organization_type already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 기존 SUPER_ADMIN, SYSTEM_ADMIN 사용자들이 SYSTEM 타입이 아닌 경우에만 업데이트
UPDATE users 
SET organization_type = 'SYSTEM', 
    organization_id = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE (JSON_CONTAINS(roles, '"SUPER_ADMIN"') = 1 
       OR JSON_CONTAINS(roles, '"SYSTEM_ADMIN"') = 1)
  AND (organization_type IS NULL OR organization_type != 'SYSTEM');

-- ========================================
-- 필요한 테이블들 생성 (존재하지 않는 경우)
-- ========================================

-- 본사 정보 테이블 생성
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

-- 매장 정보 테이블 생성
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
    INDEX idx_hq_id (hq_id),
    INDEX idx_region_code (region_code),
    INDEX idx_store_status (store_status),
    INDEX idx_manager_user_id (manager_user_id),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (hq_id) REFERENCES headquarters(hq_id) ON DELETE SET NULL,
    FOREIGN KEY (manager_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 지역 코드 테이블 생성 (stores에서 참조)
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

-- 기본 지역 코드 데이터 삽입
INSERT INTO region_codes (region_code, region_name, parent_region, display_order)
VALUES 
('001', '서울특별시', NULL, 1),
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

-- 감사 로그 테이블 생성 (트리거에서 사용)
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id         VARCHAR(36) PRIMARY KEY,
    table_name     VARCHAR(50) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    primary_key    VARCHAR(100),
    user_id        VARCHAR(36),
    changes        JSON,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_table_name (table_name),
    INDEX idx_operation_type (operation_type),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 먼저 기존 트리거가 있다면 삭제
DROP TRIGGER IF EXISTS tr_headquarters_after_insert;
DROP TRIGGER IF EXISTS tr_stores_after_insert;

-- ========================================
-- 본사 생성 시 자동 관리자 계정 생성 트리거
-- ========================================

DELIMITER $$

CREATE TRIGGER tr_headquarters_after_insert
    AFTER INSERT ON headquarters
    FOR EACH ROW
BEGIN
    DECLARE admin_user_id VARCHAR(36);
    DECLARE admin_username VARCHAR(50);
    DECLARE admin_email VARCHAR(320);
    DECLARE admin_password_hash VARCHAR(255);
    DECLARE now_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    
    -- 관리자 계정 정보 생성
    SET admin_user_id = CONCAT('HQ-', NEW.hq_id, '-ADMIN');
    SET admin_username = CONCAT(NEW.hq_code, '_admin');
    SET admin_email = CONCAT(NEW.hq_code, '_admin@', LOWER(NEW.hq_code), '.webpos.com');
    -- 기본 비밀번호: 'password123' (운영환경에서는 임시 비밀번호 생성 로직 필요)
    SET admin_password_hash = '$2a$10$RUDKfsvPLlm75vp5DVccCej2esBU4iAYxGO7Pcj3nCwSjIERLmI32';
    SET now_timestamp = NOW();
    
    -- 본사 관리자 계정 생성
    INSERT INTO users (
        id,
        username,
        email,
        password_hash,
        roles,
        user_status,
        organization_id,
        organization_type,
        is_active,
        created_at,
        created_by,
        updated_at,
        version
    ) VALUES (
        admin_user_id,
        admin_username,
        admin_email,
        admin_password_hash,
        '["HQ_MANAGER"]',
        'ACTIVE',
        NEW.hq_id,
        'HEADQUARTERS',
        TRUE,
        now_timestamp,
        NEW.created_by,
        now_timestamp,
        0
    ) ON DUPLICATE KEY UPDATE
        email = VALUES(email),
        organization_id = VALUES(organization_id),
        organization_type = VALUES(organization_type),
        updated_at = now_timestamp,
        version = version + 1;
        
    -- 본사 관리자에게 영업정보시스템 메뉴 권한 부여
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
    )
    SELECT 
        CONCAT('perm-', admin_user_id, '-', m.menu_id, '-admin'),
        m.menu_id,
        'USER',
        admin_user_id,
        'ADMIN',
        'system',
        TRUE,
        now_timestamp,
        now_timestamp
    FROM menus m 
    WHERE m.menu_code LIKE 'BUSINESS_%' 
      AND m.is_active = TRUE
    ON DUPLICATE KEY UPDATE
        permission_type = VALUES(permission_type),
        updated_at = now_timestamp;
        
    -- 트리거 실행 로그 (선택사항) - 임시로 비활성화
    /*
    INSERT INTO audit_logs (
        log_id,
        table_name,
        operation_type,
        primary_key,
        user_id,
        changes,
        created_at
    ) VALUES (
        UUID(),
        'headquarters',
        'TRIGGER_USER_CREATION',
        NEW.hq_id,
        'system',
        JSON_OBJECT(
            'hq_id', NEW.hq_id,
            'admin_user_created', admin_user_id,
            'admin_username', admin_username,
            'admin_email', admin_email
        ),
        now_timestamp
    );
    */
END$$

DELIMITER ;

-- ========================================
-- 매장 생성 시 자동 관리자 계정 생성 트리거
-- ========================================

DELIMITER $$

CREATE TRIGGER tr_stores_after_insert
    AFTER INSERT ON stores
    FOR EACH ROW
BEGIN
    DECLARE admin_user_id VARCHAR(36);
    DECLARE admin_username VARCHAR(50);
    DECLARE admin_email VARCHAR(320);
    DECLARE admin_password_hash VARCHAR(255);
    DECLARE now_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    DECLARE store_prefix VARCHAR(20);
    
    -- 매장 타입에 따른 접두사 설정
    IF NEW.store_type = 'CHAIN' THEN
        SET store_prefix = CONCAT(NEW.hq_id, '_', NEW.region_code, NEW.store_number);
    ELSE
        SET store_prefix = CONCAT('IN_', NEW.region_code, NEW.store_number);
    END IF;
    
    -- 관리자 계정 정보 생성
    SET admin_user_id = CONCAT('ST-', NEW.store_id, '-ADMIN');
    SET admin_username = CONCAT(store_prefix, '_admin');
    SET admin_email = CONCAT(store_prefix, '_admin@', 'store.webpos.com');
    -- 기본 비밀번호: 'password123' (운영환경에서는 임시 비밀번호 생성 로직 필요)
    SET admin_password_hash = '$2a$10$EqKwQKUjQQ7zMwcXCNHyWu.V.VwKSLGrjTlIr/Rvz3Yh4VWL6/QE.';
    SET now_timestamp = NOW();
    
    -- 매장 관리자 계정 생성
    INSERT INTO users (
        id,
        username,
        email,
        password_hash,
        roles,
        user_status,
        organization_id,
        organization_type,
        is_active,
        created_at,
        created_by,
        updated_at,
        version
    ) VALUES (
        admin_user_id,
        admin_username,
        admin_email,
        admin_password_hash,
        '["STORE_MANAGER"]',
        'ACTIVE',
        NEW.store_id,
        'STORE',
        TRUE,
        now_timestamp,
        NEW.created_by,
        now_timestamp,
        0
    ) ON DUPLICATE KEY UPDATE
        email = VALUES(email),
        organization_id = VALUES(organization_id),
        organization_type = VALUES(organization_type),
        updated_at = now_timestamp,
        version = version + 1;
        
    -- 매장 관리자에게 POS 시스템 메뉴 권한 부여
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
    )
    SELECT 
        CONCAT('perm-', admin_user_id, '-', m.menu_id, '-admin'),
        m.menu_id,
        'USER',
        admin_user_id,
        'ADMIN',
        'system',
        TRUE,
        now_timestamp,
        now_timestamp
    FROM menus m 
    WHERE m.menu_code LIKE 'POS_%' 
      AND m.is_active = TRUE
    ON DUPLICATE KEY UPDATE
        permission_type = VALUES(permission_type),
        updated_at = now_timestamp;
        
    -- 영업정보시스템의 POS 관리 메뉴 권한도 부여 (매장에서 POS 관리용)
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
    )
    SELECT 
        CONCAT('perm-', admin_user_id, '-', m.menu_id, '-admin'),
        m.menu_id,
        'USER',
        admin_user_id,
        'ADMIN',
        'system',
        TRUE,
        now_timestamp,
        now_timestamp
    FROM menus m 
    WHERE m.menu_code = 'BUSINESS_POS'
      AND m.is_active = TRUE
    ON DUPLICATE KEY UPDATE
        permission_type = VALUES(permission_type),
        updated_at = now_timestamp;
        
    -- stores 테이블의 manager_user_id 업데이트
    UPDATE stores 
    SET manager_user_id = admin_user_id,
        updated_at = now_timestamp,
        version = version + 1
    WHERE store_id = NEW.store_id;
        
    -- 트리거 실행 로그 (선택사항) - 임시로 비활성화
    /*
    INSERT INTO audit_logs (
        log_id,
        table_name,
        operation_type,
        primary_key,
        user_id,
        changes,
        created_at
    ) VALUES (
        UUID(),
        'stores',
        'TRIGGER_USER_CREATION',
        NEW.store_id,
        'system',
        JSON_OBJECT(
            'store_id', NEW.store_id,
            'store_type', NEW.store_type,
            'admin_user_created', admin_user_id,
            'admin_username', admin_username,
            'admin_email', admin_email
        ),
        now_timestamp
    );
    */
END$$

DELIMITER ;

-- ========================================
-- 영업정보시스템 POS 관리 메뉴 추가
-- ========================================

-- 영업정보시스템에 POS 관리 메뉴 추가 (매장 관리자용)
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
    'menu-business-pos',
    'BUSINESS_POS',
    'POS 관리',
    '/business/pos',
    'menu-business',
    'MENU',
    2,
    45,
    'computer-desktop',
    '매장의 POS 기기를 관리합니다',
    TRUE,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    menu_level = 2,
    display_order = 45,
    updated_at = NOW();

-- ========================================
-- 트리거 테스트용 임시 데이터 (개발/테스트 환경에서만 사용)
-- ========================================

-- 테스트용 본사 데이터 삽입 (트리거 동작 확인용)
INSERT INTO headquarters (
    hq_id,
    hq_code,
    hq_name,
    business_license,
    ceo_name,
    headquarters_address,
    contact_phone,
    website,
    is_active,
    created_by
) VALUES (
    'HQ001',
    'TEST',
    '테스트 본사',
    '123-45-67890',
    '김본사',
    '서울시 강남구 테스트로 123',
    '02-123-4567',
    'https://test.webpos.com',
    TRUE,
    'admin'
) ON DUPLICATE KEY UPDATE
    hq_name = VALUES(hq_name),
    updated_at = NOW();

-- 테스트용 매장 데이터 삽입 (트리거 동작 확인용)
INSERT INTO stores (
    store_id,
    store_name,
    store_type,
    operation_type,
    hq_id,
    region_code,
    store_number,
    business_license,
    owner_name,
    phone_number,
    address,
    postal_code,
    opening_date,
    store_status,
    is_active,
    created_by
) VALUES (
    'HQ001001',
    '테스트 강남점',
    'CHAIN',
    'FRANCHISE',
    'HQ001',
    '001',
    '001',
    '234-56-78901',
    '김매장',
    '02-234-5678',
    '서울시 강남구 매장로 456',
    '12345',
    CURDATE(),
    'ACTIVE',
    TRUE,
    'HQ-HQ001-ADMIN'
) ON DUPLICATE KEY UPDATE
    store_name = VALUES(store_name),
    updated_at = NOW();

-- ========================================
-- 권한 정리 및 업데이트
-- ========================================

-- STORE_MANAGER 역할에 영업정보시스템 POS 관리 권한 부여
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
) VALUES (
    'perm-store-manager-business-pos-admin',
    'menu-business-pos',
    'ROLE',
    'STORE_MANAGER',
    'ADMIN',
    'system',
    TRUE,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- HQ_MANAGER 역할에도 POS 관리 권한 부여 (본사에서 전체 POS 관리)
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
) VALUES (
    'perm-hq-manager-business-pos-admin',
    'menu-business-pos',
    'ROLE',
    'HQ_MANAGER',
    'ADMIN',
    'system',
    TRUE,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    permission_type = VALUES(permission_type),
    updated_at = NOW();

-- ========================================
-- 트리거 상태 확인 뷰 생성
-- ========================================

-- 트리거 동작 상태를 확인할 수 있는 뷰
CREATE OR REPLACE VIEW trigger_status AS
SELECT 
    TRIGGER_SCHEMA,
    TRIGGER_NAME,
    EVENT_MANIPULATION,
    EVENT_OBJECT_TABLE,
    ACTION_TIMING,
    CREATED
FROM information_schema.TRIGGERS 
WHERE TRIGGER_SCHEMA = DATABASE()
  AND TRIGGER_NAME IN ('tr_headquarters_after_insert', 'tr_stores_after_insert');

-- ========================================
-- 조직별 사용자 현황 뷰 생성
-- ========================================

-- 조직별 사용자 현황을 확인할 수 있는 뷰
CREATE OR REPLACE VIEW organization_users AS
SELECT 
    u.organization_type,
    u.organization_id,
    CASE 
        WHEN u.organization_type = 'HEADQUARTERS' THEN 
            (SELECT h.hq_name FROM headquarters h WHERE h.hq_id = u.organization_id)
        WHEN u.organization_type = 'STORE' THEN 
            (SELECT s.store_name FROM stores s WHERE s.store_id = u.organization_id)
        ELSE 'SYSTEM'
    END as organization_name,
    COUNT(*) as user_count,
    GROUP_CONCAT(u.username ORDER BY u.created_at) as usernames,
    GROUP_CONCAT(
        CONCAT('[', 
            REPLACE(REPLACE(REPLACE(u.roles, '[', ''), ']', ''), '"', ''), 
        ']') 
        ORDER BY u.created_at
    ) as user_roles
FROM users u 
WHERE u.is_active = TRUE
GROUP BY u.organization_type, u.organization_id
ORDER BY u.organization_type, u.organization_id;

-- ========================================
-- 마이그레이션 완료 로그 - 임시로 비활성화
-- ========================================

/*
INSERT INTO audit_logs (
    log_id,
    table_name,
    operation_type,
    primary_key,
    user_id,
    changes,
    created_at
) VALUES (
    UUID(),
    'system',
    'MIGRATION',
    'V8',
    'system',
    JSON_OBJECT(
        'migration_version', 'V8',
        'description', 'Auto user creation triggers added',
        'features', JSON_ARRAY(
            'headquarters_trigger',
            'stores_trigger', 
            'business_pos_menu',
            'organization_users_view',
            'trigger_status_view'
        )
    ),
    NOW()
);
*/

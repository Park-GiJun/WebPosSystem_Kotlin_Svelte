-- audit_logs 테이블 구조 확인 및 수정

-- 1. 현재 테이블 구조 확인
DESCRIBE audit_logs;

-- 2. 테이블이 없다면 생성
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    audit_id VARCHAR(36) NOT NULL UNIQUE DEFAULT (UUID()),
    table_name VARCHAR(50) NOT NULL,
    record_id VARCHAR(50) NOT NULL,
    action_type ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    changed_fields JSON NULL,
    user_id VARCHAR(36) NULL,
    user_name VARCHAR(50) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    session_id VARCHAR(100) NULL,
    request_uri VARCHAR(500) NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_table_name (table_name),
    INDEX idx_record_id (record_id),
    INDEX idx_action_type (action_type),
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_name),
    INDEX idx_created_at (created_at),
    INDEX idx_audit_id (audit_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. action_type 컬럼이 없다면 추가
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS action_type ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL DEFAULT 'INSERT' 
AFTER record_id;

-- 4. 다른 필요한 컬럼들도 확인 후 추가
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS old_values JSON NULL AFTER action_type;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS new_values JSON NULL AFTER old_values;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS changed_fields JSON NULL AFTER new_values;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS user_id VARCHAR(36) NULL AFTER changed_fields;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS user_name VARCHAR(50) NULL AFTER user_id;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45) NULL AFTER user_name;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS user_agent TEXT NULL AFTER ip_address;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS session_id VARCHAR(100) NULL AFTER user_agent;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS request_uri VARCHAR(500) NULL AFTER session_id;

ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS description VARCHAR(500) NULL AFTER request_uri;

-- 5. 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_action_type ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_name ON audit_logs(user_name);
CREATE INDEX IF NOT EXISTS idx_created_at ON audit_logs(created_at);

-- 6. 테스트 데이터 추가
INSERT IGNORE INTO audit_logs (
    audit_id, table_name, record_id, action_type, 
    user_id, user_name, ip_address, description, created_at
) VALUES 
('test-audit-1', 'users', 'admin', 'INSERT', 
 'system', 'system', '127.0.0.1', '시스템 관리자 계정 생성', 
 DATE_SUB(NOW(), INTERVAL 30 DAY)),

('test-audit-2', 'users', 'store_admin_001', 'INSERT',
 'admin', 'admin', '192.168.1.100', '매장 관리자 계정 생성',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

('test-audit-3', 'stores', 'IN001001', 'INSERT',
 'admin', 'admin', '192.168.1.100', '개인매장 생성: 강남 편의점',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

('test-audit-4', 'stores', 'IN001001', 'UPDATE',
 'store_admin_001', 'gangnam_store', '192.168.1.101', '매장 전화번호 수정',
 DATE_SUB(NOW(), INTERVAL 5 DAY)),

('test-audit-5', 'permissions', 'perm_user_001', 'INSERT',
 'admin', 'admin', '192.168.1.100', 'POS 판매 권한 부여',
 DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 7. 최종 테이블 구조 확인
DESCRIBE audit_logs;

-- 8. 데이터 확인
SELECT COUNT(*) as total_records FROM audit_logs;
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 5;

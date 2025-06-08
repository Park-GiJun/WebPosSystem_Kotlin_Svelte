-- audit_logs 테이블을 코드에 맞게 완전히 재생성

-- 기존 테이블 백업 (혹시 데이터가 있다면)
CREATE TABLE IF NOT EXISTS audit_logs_backup AS SELECT * FROM audit_logs;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS audit_logs;

-- 새로운 테이블 생성 (코드와 완전히 일치하도록)
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    audit_id VARCHAR(36) NOT NULL UNIQUE DEFAULT (UUID()),
    table_name VARCHAR(50) NOT NULL,
    record_id VARCHAR(50) NOT NULL,
    action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
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
    INDEX idx_action (action),
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_name),
    INDEX idx_created_at (created_at),
    INDEX idx_audit_id (audit_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 테스트 데이터 삽입
INSERT INTO audit_logs (
    audit_id, table_name, record_id, action, 
    user_id, user_name, ip_address, description, created_at
) VALUES 
(UUID(), 'users', 'admin', 'INSERT', 
 'system', 'system', '127.0.0.1', '시스템 관리자 계정 생성', 
 DATE_SUB(NOW(), INTERVAL 30 DAY)),

(UUID(), 'users', 'store_admin_001', 'INSERT',
 'admin', 'admin', '192.168.1.100', '매장 관리자 계정 생성',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

(UUID(), 'stores', 'IN001001', 'INSERT',
 'admin', 'admin', '192.168.1.100', '개인매장 생성: 강남 편의점',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

(UUID(), 'stores', 'IN001001', 'UPDATE',
 'store_admin_001', 'gangnam_store', '192.168.1.101', '매장 전화번호 수정',
 DATE_SUB(NOW(), INTERVAL 5 DAY)),

(UUID(), 'permissions', 'perm_user_001', 'INSERT',
 'admin', 'admin', '192.168.1.100', 'POS 판매 권한 부여',
 DATE_SUB(NOW(), INTERVAL 10 DAY)),

(UUID(), 'users', 'test_user_001', 'UPDATE',
 'admin', 'admin', '192.168.1.105', '사용자 정보 수정',
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

(UUID(), 'stores', 'FR002001', 'INSERT',
 'admin', 'admin', '192.168.1.100', '프랜차이즈 매장 등록',
 DATE_SUB(NOW(), INTERVAL 8 DAY)),

(UUID(), 'permissions', 'perm_user_002', 'DELETE',
 'admin', 'admin', '192.168.1.100', '권한 삭제',
 DATE_SUB(NOW(), INTERVAL 2 DAY)),

(UUID(), 'users', 'cashier_001', 'INSERT',
 'store_admin_001', 'gangnam_store', '192.168.1.102', '계산원 계정 생성',
 DATE_SUB(NOW(), INTERVAL 1 DAY)),

(UUID(), 'stores', 'IN001001', 'UPDATE',
 'admin', 'admin', '192.168.1.100', '매장 운영시간 변경',
 NOW());

-- 테이블 구조 확인
DESCRIBE audit_logs;

-- 데이터 확인
SELECT COUNT(*) as total_records FROM audit_logs;
SELECT 
    table_name,
    action,
    COUNT(*) as count
FROM audit_logs 
GROUP BY table_name, action 
ORDER BY table_name, action;

-- 최근 데이터 확인
SELECT 
    audit_id,
    table_name,
    record_id,
    action,
    user_name,
    description,
    created_at
FROM audit_logs 
ORDER BY created_at DESC 
LIMIT 10;

-- 누락된 컬럼들을 audit_logs 테이블에 추가

-- audit_id 컬럼 추가 (UUID)
ALTER TABLE audit_logs 
ADD COLUMN audit_id VARCHAR(36) NOT NULL DEFAULT (UUID()) 
AFTER id;

-- user_name 컬럼 추가
ALTER TABLE audit_logs 
ADD COLUMN user_name VARCHAR(50) NULL 
AFTER user_id;

-- session_id 컬럼 추가
ALTER TABLE audit_logs 
ADD COLUMN session_id VARCHAR(100) NULL 
AFTER user_agent;

-- request_uri 컬럼 추가
ALTER TABLE audit_logs 
ADD COLUMN request_uri VARCHAR(500) NULL 
AFTER session_id;

-- description 컬럼 추가
ALTER TABLE audit_logs 
ADD COLUMN description VARCHAR(500) NULL 
AFTER request_uri;

-- audit_id에 고유 인덱스 추가
CREATE UNIQUE INDEX idx_audit_id ON audit_logs(audit_id);

-- 기존 레코드들에 audit_id 값 설정
UPDATE audit_logs SET audit_id = UUID() WHERE audit_id IS NULL OR audit_id = '';

-- 테이블 구조 확인
DESCRIBE audit_logs;

-- 테스트 데이터 추가
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
 DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 데이터 확인
SELECT COUNT(*) as total_records FROM audit_logs;
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 5;

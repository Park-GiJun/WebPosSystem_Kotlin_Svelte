-- 간단한 audit_logs 테이블 수정 (MySQL 버전 호환성)

-- 현재 테이블 구조 확인
DESCRIBE audit_logs;

-- action_type 컬럼 추가 (이미 있으면 에러 발생하지만 무시)
ALTER TABLE audit_logs 
ADD COLUMN action_type ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL DEFAULT 'INSERT' 
AFTER record_id;

-- 필요한 다른 컬럼들 추가
ALTER TABLE audit_logs ADD COLUMN old_values JSON NULL;
ALTER TABLE audit_logs ADD COLUMN new_values JSON NULL; 
ALTER TABLE audit_logs ADD COLUMN changed_fields JSON NULL;
ALTER TABLE audit_logs ADD COLUMN user_id VARCHAR(36) NULL;
ALTER TABLE audit_logs ADD COLUMN user_name VARCHAR(50) NULL;
ALTER TABLE audit_logs ADD COLUMN ip_address VARCHAR(45) NULL;
ALTER TABLE audit_logs ADD COLUMN user_agent TEXT NULL;
ALTER TABLE audit_logs ADD COLUMN session_id VARCHAR(100) NULL;
ALTER TABLE audit_logs ADD COLUMN request_uri VARCHAR(500) NULL;
ALTER TABLE audit_logs ADD COLUMN description VARCHAR(500) NULL;

-- 인덱스 추가
CREATE INDEX idx_action_type ON audit_logs(action_type);
CREATE INDEX idx_user_name ON audit_logs(user_name);
CREATE INDEX idx_created_at ON audit_logs(created_at);

-- 테스트 데이터
INSERT INTO audit_logs (
    table_name, record_id, action_type, user_name, description
) VALUES 
('users', 'test1', 'INSERT', 'admin', '테스트 사용자 생성'),
('stores', 'test2', 'UPDATE', 'manager', '테스트 매장 수정'),
('permissions', 'test3', 'DELETE', 'admin', '테스트 권한 삭제');

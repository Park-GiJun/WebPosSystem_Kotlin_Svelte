-- V16__test_audit_system.sql
-- 감사 로그 시스템 테스트

-- 기존 admin 사용자 조직 정보 업데이트 (트리거 테스트)
UPDATE users 
SET organization_type = 'SYSTEM',
    updated_at = NOW(),
    updated_by = 'system'
WHERE username = 'admin';

-- 테스트용 매장 상태 변경 (트리거 테스트)
UPDATE stores 
SET store_status = 'ACTIVE',
    updated_at = NOW(),
    updated_by = 'admin'
WHERE store_id = 'IN001001';

-- 권한 업데이트 (트리거 테스트)
UPDATE permissions 
SET is_active = true,
    updated_at = NOW()
WHERE target_id = 'ADMIN' 
AND menu_code = 'ADMIN_AUDIT';

-- 추가 테스트 데이터 생성 (다양한 시간대)
INSERT INTO audit_logs (
    table_name, record_id, action_type, new_values, user_id, user_name, 
    ip_address, description, created_at
) VALUES 
-- 1시간 전
('users', 'test_user_001', 'INSERT',
 JSON_OBJECT('username', 'test_user', 'email', 'test@example.com', 'roles', 'USER'),
 'admin', 'admin', '192.168.1.50', '테스트 사용자 계정 생성',
 DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- 30분 전
('stores', 'IN001003', 'INSERT',
 JSON_OBJECT('store_name', '테스트 매장', 'store_type', 'INDIVIDUAL', 'owner_name', '테스트'),
 'admin', 'admin', '192.168.1.50', '테스트 매장 생성',
 DATE_SUB(NOW(), INTERVAL 30 MINUTE)),

-- 15분 전
('stores', 'IN001003', 'UPDATE',
 JSON_OBJECT('store_name', '테스트 매장', 'phone_number', '02-9999-8888'),
 JSON_OBJECT('store_name', '테스트 매장 (수정)', 'phone_number', '02-9999-8888'),
 JSON_OBJECT('store_name', JSON_OBJECT('old', '테스트 매장', 'new', '테스트 매장 (수정)')),
 'admin', 'admin', '192.168.1.50', '매장명 수정',
 DATE_SUB(NOW(), INTERVAL 15 MINUTE)),

-- 5분 전
('permissions', 'test_perm_001', 'INSERT',
 JSON_OBJECT('menu_code', 'POS_SALES', 'target_id', 'test_user_001', 'permission_type', 'READ'),
 'admin', 'admin', '192.168.1.50', '사용자 권한 부여',
 DATE_SUB(NOW(), INTERVAL 5 MINUTE)),

-- 현재
('users', 'test_user_001', 'UPDATE',
 JSON_OBJECT('user_status', 'ACTIVE', 'email_verified_at', NOW()),
 JSON_OBJECT('user_status', 'ACTIVE', 'email_verified_at', NOW()),
 JSON_OBJECT('email_verified_at', JSON_OBJECT('old', NULL, 'new', NOW())),
 'admin', 'admin', '192.168.1.50', '이메일 인증 완료',
 NOW());

-- 통계 확인용 쿼리 (주석으로 남겨둠)
/*
-- 액션별 통계
SELECT action_type, COUNT(*) as count 
FROM audit_logs 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY action_type;

-- 테이블별 통계
SELECT table_name, COUNT(*) as count 
FROM audit_logs 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY table_name 
ORDER BY count DESC;

-- 사용자별 통계
SELECT user_name, COUNT(*) as count 
FROM audit_logs 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) 
AND user_name IS NOT NULL
GROUP BY user_name 
ORDER BY count DESC;

-- 최근 감사 로그 확인
SELECT * FROM audit_logs 
ORDER BY created_at DESC 
LIMIT 10;
*/

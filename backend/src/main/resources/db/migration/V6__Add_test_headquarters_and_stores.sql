-- V6: 테스트용 본사 및 사용자 데이터 추가

-- 기존 head3 사용자를 본사 관리자로 업데이트
UPDATE users 
SET organization_id = 'HQSTT', 
    organization_type = 'HEADQUARTERS',
    roles = '["HQ_MANAGER"]'
WHERE username = 'head3';

-- head3 사용자가 없다면 생성
INSERT INTO users (
    id, 
    username, 
    email, 
    password_hash, 
    roles, 
    user_status, 
    organization_id, 
    organization_type, 
    email_verified_at, 
    is_active, 
    created_at, 
    updated_at, 
    version
) VALUES (
    'hq-manager-id-001',
    'head3',
    'head3@company.com',
    '$2a$10$RUDKfsvPLlm75vp5DVccCej2esBU4iAYxGO7Pcj3nCwSjIERLmI32', -- password123
    '["HQ_MANAGER"]',
    'ACTIVE',
    'HQSTT',
    'HEADQUARTERS',
    NOW(),
    true,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE 
    organization_id = VALUES(organization_id),
    organization_type = VALUES(organization_type),
    roles = VALUES(roles);

-- 본사에 권한 추가 (누락된 권한들)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-hq-manager-business-hq', 'write', 'ROLE', 'BUSINESS_HQ', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

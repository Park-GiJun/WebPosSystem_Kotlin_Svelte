-- ========================================
-- V9: 매장 관리자 사용자 ID 업데이트
-- ========================================

-- 기존 매장들의 manager_user_id를 해당 매장의 관리자 계정으로 업데이트
UPDATE stores s
SET manager_user_id = CONCAT('ST-', s.store_id, '-ADMIN'),
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE s.manager_user_id IS NULL
  AND EXISTS (
      SELECT 1 FROM users u 
      WHERE u.id = CONCAT('ST-', s.store_id, '-ADMIN')
        AND u.organization_type = 'STORE'
        AND u.organization_id = s.store_id
        AND u.is_active = TRUE
  );

-- 트리거 테스트용 데이터 삽입 (이제 안전하게 삽입 가능)
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

-- 방금 삽입된 매장의 manager_user_id 업데이트
UPDATE stores 
SET manager_user_id = 'ST-HQ001001-ADMIN',
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE store_id = 'HQ001001'
  AND manager_user_id IS NULL;

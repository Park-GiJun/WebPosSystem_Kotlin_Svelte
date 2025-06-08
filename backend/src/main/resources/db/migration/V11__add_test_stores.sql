-- V11__add_test_stores.sql
-- 테스트용 매장 데이터 추가

-- 개인매장 샘플 데이터
INSERT INTO stores (
    store_id, store_name, store_type, operation_type, hq_id, 
    region_code, store_number, business_license, owner_name, 
    phone_number, address, postal_code, opening_date, 
    store_status, is_active, created_at, created_by
) VALUES 
-- 서울 개인매장
('IN001001', '강남 편의점', 'INDIVIDUAL', NULL, NULL,
 '001', '001', '123-45-67890', '김편의', 
 '02-1234-5678', '서울특별시 강남구 테헤란로 123', '06142', '2023-01-15',
 'ACTIVE', true, NOW(), 'admin'),

('IN001002', '홍대 카페', 'INDIVIDUAL', NULL, NULL,
 '001', '002', '234-56-78901', '이카페', 
 '02-2345-6789', '서울특별시 마포구 홍익로 45', '04066', '2023-02-20',
 'ACTIVE', true, NOW(), 'admin'),

-- 부산 개인매장 
('IN002001', '해운대 치킨집', 'INDIVIDUAL', NULL, NULL,
 '002', '001', '345-67-89012', '박치킨', 
 '051-3456-7890', '부산광역시 해운대구 해운대로 567', '48094', '2023-03-10',
 'ACTIVE', true, NOW(), 'admin'),

-- 경기도 개인매장
('IN009001', '분당 베이커리', 'INDIVIDUAL', NULL, NULL,
 '009', '001', '456-78-90123', '최빵집', 
 '031-4567-8901', '경기도 성남시 분당구 판교로 789', '13494', '2023-04-05',
 'ACTIVE', true, NOW(), 'admin'),

('IN009002', '수원 PC방', 'INDIVIDUAL', NULL, NULL,
 '009', '002', '567-89-01234', '정게임', 
 '031-5678-9012', '경기도 수원시 영통구 광교로 321', '16499', '2023-05-12',
 'ACTIVE', true, NOW(), 'admin');

-- 체인점을 위한 본사 데이터는 별도 테이블이 필요하므로 임시로 체인점 데이터는 제외
-- 실제 운영시에는 headquarters 테이블을 만들고 체인점 데이터를 추가해야 함

-- 매장 관리자 계정 생성 (각 개인매장별로)
INSERT INTO users (
    id, username, email, password_hash, roles, organization_id, organization_type,
    user_status, is_active, created_at, created_by
) VALUES
-- 강남 편의점 관리자
('store_admin_001', 'gangnam_store', 'gangnam@example.com', 
 '$2a$10$N.zmdr9k7uOCQb0VeSsEZ.b9OQsLelqQhRLnHBfSvOwpSKtjq6cTu', -- temp123!
 'STORE_ADMIN', 'IN001001', 'INDIVIDUAL_STORE',
 'ACTIVE', true, NOW(), 'admin'),

-- 홍대 카페 관리자  
('store_admin_002', 'hongdae_cafe', 'hongdae@example.com',
 '$2a$10$N.zmdr9k7uOCQb0VeSsEZ.b9OQsLelqQhRLnHBfSvOwpSKtjq6cTu', -- temp123!
 'STORE_ADMIN', 'IN001002', 'INDIVIDUAL_STORE', 
 'ACTIVE', true, NOW(), 'admin'),

-- 해운대 치킨집 관리자
('store_admin_003', 'haeundae_chicken', 'haeundae@example.com',
 '$2a$10$N.zmdr9k7uOCQb0VeSsEZ.b9OQsLelqQhRLnHBfSvOwpSKtjq6cTu', -- temp123!
 'STORE_ADMIN', 'IN002001', 'INDIVIDUAL_STORE',
 'ACTIVE', true, NOW(), 'admin'),

-- 분당 베이커리 관리자
('store_admin_004', 'bundang_bakery', 'bundang@example.com',
 '$2a$10$N.zmdr9k7uOCQb0VeSsEZ.b9OQsLelqQhRLnHBfSvOwpSKtjq6cTu', -- temp123!
 'STORE_ADMIN', 'IN009001', 'INDIVIDUAL_STORE',
 'ACTIVE', true, NOW(), 'admin'),

-- 수원 PC방 관리자
('store_admin_005', 'suwon_pcroom', 'suwon@example.com',
 '$2a$10$N.zmdr9k7uOCQb0VeSsEZ.b9OQsLelqQhRLnHBfSvOwpSKtjq6cTu', -- temp123!
 'STORE_ADMIN', 'IN009002', 'INDIVIDUAL_STORE',
 'ACTIVE', true, NOW(), 'admin');

-- 각 매장에 대한 권한 부여
INSERT INTO permissions (
    id, permission_type, permission_target_type, menu_code, 
    target_id, granted_by, is_active, created_at
)
SELECT 
    CONCAT('store_perm_', ROW_NUMBER() OVER()), 
    'READ', 'USER', 'BUSINESS_STORES',
    u.id, 'admin', true, NOW()
FROM users u 
WHERE u.organization_type = 'INDIVIDUAL_STORE' 
AND u.roles = 'STORE_ADMIN';

-- V3: 영업정보시스템에 상품 관리 메뉴 추가

-- 영업정보시스템 상품 관리 메뉴 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, menu_type, parent_menu_id, icon_name, display_order, is_active, description, created_at, created_by)
VALUES 
('menu-business-products', 'BUSINESS_PRODUCTS', '상품 관리', '/business/products', 'MENU', 'menu-business', 'package', 5, TRUE, '매장의 상품을 등록하고 관리합니다', CURRENT_TIMESTAMP, 'system')
ON DUPLICATE KEY UPDATE menu_name = menu_name;

-- 매장 관리자에게 상품 관리 권한 부여 (permissions 테이블 사용)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
(UUID(), 'read', 'ROLE', 'BUSINESS_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'write', 'ROLE', 'BUSINESS_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'delete', 'ROLE', 'BUSINESS_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'admin', 'ROLE', 'BUSINESS_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 본사 관리자에게도 상품 관리 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
(UUID(), 'read', 'ROLE', 'BUSINESS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'write', 'ROLE', 'BUSINESS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'delete', 'ROLE', 'BUSINESS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'admin', 'ROLE', 'BUSINESS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 시스템 관리자에게도 상품 관리 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
(UUID(), 'read', 'ROLE', 'BUSINESS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'write', 'ROLE', 'BUSINESS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'delete', 'ROLE', 'BUSINESS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'admin', 'ROLE', 'BUSINESS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 슈퍼 관리자에게도 상품 관리 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
(UUID(), 'admin', 'ROLE', 'BUSINESS_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

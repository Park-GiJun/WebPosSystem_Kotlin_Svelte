-- V15: 레벨 3 메뉴 구조 완성 및 누락된 메뉴 추가

-- 1. 누락된 레벨 3 메뉴들 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, description, menu_type, is_active, created_at, created_by) VALUES

-- POS 시스템 - 상품 관리 하위 메뉴 (누락된 것들)
('menu-pos-products-catalog', 'POS_PRODUCTS_CATALOG', '상품 목록', '/pos/products/catalog', 'menu-pos-products', 3, 10, 'package', 'POS 상품 목록 조회', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-products-search', 'POS_PRODUCTS_SEARCH', '상품 검색', '/pos/products/search', 'menu-pos-products', 3, 20, 'search', '상품 빠른 검색', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),

-- POS 시스템 - 판매 하위 메뉴 (누락된 것들)  
('menu-pos-sales-register', 'POS_SALES_REGISTER', '판매 등록', '/pos/sales/register', 'menu-pos-sales', 3, 10, 'shopping-cart', '상품 판매 등록', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-sales-history', 'POS_SALES_HISTORY', '판매 내역', '/pos/sales/history', 'menu-pos-sales', 3, 20, 'clock', '판매 이력 조회', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-sales-returns', 'POS_SALES_RETURNS', '반품/교환', '/pos/sales/returns', 'menu-pos-sales', 3, 30, 'refresh-ccw', '반품 및 교환 처리', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),

-- POS 시스템 - 설정 하위 메뉴 (누락된 것들)
('menu-pos-settings-device', 'POS_SETTINGS_DEVICE', '기기 설정', '/pos/settings/device', 'menu-pos-settings', 3, 10, 'monitor', 'POS 기기 환경 설정', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-settings-printer', 'POS_SETTINGS_PRINTER', '프린터 설정', '/pos/settings/printer', 'menu-pos-settings', 3, 20, 'printer', '영수증 프린터 설정', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-settings-payment', 'POS_SETTINGS_PAYMENT', '결제 설정', '/pos/settings/payment', 'menu-pos-settings', 3, 30, 'credit-card', '결제 수단 설정', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),

-- 영업정보시스템 - 통계/보고서 하위 메뉴 (누락된 것들)
('menu-business-reports-sales', 'BUSINESS_REPORTS_SALES', '매출 분석', '/business/reports/sales', 'menu-business-reports', 3, 10, 'bar-chart', '매출 통계 및 분석', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-reports-products', 'BUSINESS_REPORTS_PRODUCTS', '상품 분석', '/business/reports/products', 'menu-business-reports', 3, 20, 'package', '상품별 통계 분석', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-reports-customers', 'BUSINESS_REPORTS_CUSTOMERS', '고객 분석', '/business/reports/customers', 'menu-business-reports', 3, 30, 'user', '고객 통계 분석', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),

-- 영업정보시스템 - 매장 관리 하위 메뉴 (누락된 것들)
('menu-business-stores-info', 'BUSINESS_STORES_INFO', '매장 정보', '/business/stores/info', 'menu-business-stores', 3, 10, 'info', '매장 기본 정보 관리', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-stores-staff', 'BUSINESS_STORES_STAFF', '매장 직원', '/business/stores/staff', 'menu-business-stores', 3, 20, 'users', '매장 직원 관리', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-stores-inventory', 'BUSINESS_STORES_INVENTORY', '재고 관리', '/business/stores/inventory', 'menu-business-stores', 3, 30, 'package', '매장 재고 관리', 'MENU', TRUE, CURRENT_TIMESTAMP, 'system')

ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    menu_path = VALUES(menu_path),
    description = VALUES(description),
    icon_name = VALUES(icon_name),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 2. 기존 레벨 2 메뉴들의 타입을 CATEGORY로 변경 (하위 메뉴가 있는 경우)
UPDATE menus SET 
    menu_type = 'CATEGORY',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE menu_code IN (
    'ADMIN_ORGANIZATIONS',  -- 본사/매장 관리 하위 메뉴 있음
    'ADMIN_USERS',          -- 내부/고객 사용자 하위 메뉴 있음
    'BUSINESS_REPORTS',     -- 매출/상품/고객 분석 하위 메뉴 있음  
    'BUSINESS_STORES',      -- 매장정보/직원/재고 하위 메뉴 있음
    'POS_PRODUCTS',         -- 상품목록/검색 하위 메뉴 있음
    'POS_SALES',            -- 판매등록/내역/반품 하위 메뉴 있음
    'POS_SETTINGS'          -- 기기/프린터/결제 설정 하위 메뉴 있음
);

-- 3. 새로운 레벨 3 메뉴들에 대한 권한 설정

-- SUPER_ADMIN에게 모든 새 메뉴 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- POS 상품 관리 권한
('perm-superadmin-pos-products-catalog', 'admin', 'ROLE', 'POS_PRODUCTS_CATALOG', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-products-search', 'admin', 'ROLE', 'POS_PRODUCTS_SEARCH', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- POS 판매 관리 권한
('perm-superadmin-pos-sales-register', 'admin', 'ROLE', 'POS_SALES_REGISTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-sales-history', 'admin', 'ROLE', 'POS_SALES_HISTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-sales-returns', 'admin', 'ROLE', 'POS_SALES_RETURNS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- POS 설정 권한
('perm-superadmin-pos-settings-device', 'admin', 'ROLE', 'POS_SETTINGS_DEVICE', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-settings-printer', 'admin', 'ROLE', 'POS_SETTINGS_PRINTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-settings-payment', 'admin', 'ROLE', 'POS_SETTINGS_PAYMENT', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 영업정보시스템 - 보고서 권한
('perm-superadmin-business-reports-sales', 'admin', 'ROLE', 'BUSINESS_REPORTS_SALES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-reports-products', 'admin', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-reports-customers', 'admin', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 영업정보시스템 - 매장 관리 권한
('perm-superadmin-business-stores-info', 'admin', 'ROLE', 'BUSINESS_STORES_INFO', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-stores-staff', 'admin', 'ROLE', 'BUSINESS_STORES_STAFF', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-stores-inventory', 'admin', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 4. HQ_ADMIN에게 적절한 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- 영업정보시스템 보고서 권한 (읽기 전용)
('perm-hqadmin-business-reports-sales', 'read', 'ROLE', 'BUSINESS_REPORTS_SALES', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-reports-products', 'read', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-reports-customers', 'read', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 영업정보시스템 매장 관리 권한
('perm-hqadmin-business-stores-info', 'write', 'ROLE', 'BUSINESS_STORES_INFO', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-stores-staff', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-stores-inventory', 'read', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 5. STORE_MANAGER에게 적절한 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- POS 상품 관리 권한 (읽기 전용)
('perm-storemanager-pos-products-catalog', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-products-search', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- POS 판매 관리 권한
('perm-storemanager-pos-sales-register', 'write', 'ROLE', 'POS_SALES_REGISTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-sales-history', 'read', 'ROLE', 'POS_SALES_HISTORY', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-sales-returns', 'write', 'ROLE', 'POS_SALES_RETURNS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- POS 설정 권한 (읽기 전용)
('perm-storemanager-pos-settings-device', 'read', 'ROLE', 'POS_SETTINGS_DEVICE', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-settings-printer', 'read', 'ROLE', 'POS_SETTINGS_PRINTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-settings-payment', 'read', 'ROLE', 'POS_SETTINGS_PAYMENT', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 영업정보시스템 매장 관리 권한
('perm-storemanager-business-stores-info', 'read', 'ROLE', 'BUSINESS_STORES_INFO', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-business-stores-staff', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-business-stores-inventory', 'write', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 6. POS_USER에게 기본 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- POS 상품 관리 권한 (읽기 전용)
('perm-posuser-pos-products-catalog', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-products-search', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- POS 판매 관리 권한
('perm-posuser-pos-sales-register', 'write', 'ROLE', 'POS_SALES_REGISTER', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-sales-history', 'read', 'ROLE', 'POS_SALES_HISTORY', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-sales-returns', 'write', 'ROLE', 'POS_SALES_RETURNS', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 7. 업데이트된 메뉴 수 확인을 위한 뷰 생성
CREATE OR REPLACE VIEW menu_hierarchy_summary AS
SELECT 
    menu_level,
    COUNT(*) as menu_count,
    COUNT(CASE WHEN is_active = TRUE THEN 1 END) as active_count,
    COUNT(CASE WHEN menu_type = 'CATEGORY' THEN 1 END) as category_count,
    COUNT(CASE WHEN menu_type = 'MENU' THEN 1 END) as menu_count_type
FROM menus 
WHERE deleted_at IS NULL
GROUP BY menu_level
ORDER BY menu_level;

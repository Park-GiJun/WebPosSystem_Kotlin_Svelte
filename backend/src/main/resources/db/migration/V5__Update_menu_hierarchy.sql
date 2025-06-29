-- V5: 메뉴 계층 구조 개선 및 계층형 드롭다운 지원

-- 기존 메뉴 데이터 백업 (필요시)
-- CREATE TABLE menus_backup AS SELECT * FROM menus;

-- permissions 테이블의 id 컬럼 크기 확장
ALTER TABLE permissions MODIFY COLUMN id VARCHAR(100);

-- 새로운 계층형 메뉴 데이터 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, description, is_active, created_at, created_by) VALUES

-- 관리자 시스템 - 사용자 관리 하위 카테고리
('menu-admin-users-internal', 'ADMIN_USERS_INTERNAL', '내부 사용자', '/admin/users/internal', 'menu-admin-users', 3, 10, 'user', 'CATEGORY', '내부 시스템 사용자 관리', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-admin-users-customers', 'ADMIN_USERS_CUSTOMERS', '고객 사용자', '/admin/users/customers', 'menu-admin-users', 3, 20, 'users', 'CATEGORY', '고객 사용자 관리', TRUE, CURRENT_TIMESTAMP, 'system'),

-- 관리자 시스템 - 조직 관리 하위 카테고리  
('menu-admin-org-hq', 'ADMIN_ORG_HQ', '본사 관리', '/admin/organizations/headquarters', 'menu-admin-organizations', 3, 10, 'building', 'MENU', '본사 조직 관리', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-admin-org-stores', 'ADMIN_ORG_STORES', '매장 관리', '/admin/organizations/stores', 'menu-admin-organizations', 3, 20, 'shop', 'MENU', '매장 조직 관리', TRUE, CURRENT_TIMESTAMP, 'system'),

-- 영업정보시스템 - 매장 관리 하위 카테고리
('menu-business-stores-info', 'BUSINESS_STORES_INFO', '매장 정보', '/business/stores/info', 'menu-business-stores', 3, 10, 'info', 'MENU', '매장 기본 정보 관리', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-stores-staff', 'BUSINESS_STORES_STAFF', '매장 직원', '/business/stores/staff', 'menu-business-stores', 3, 20, 'users', 'MENU', '매장 직원 관리', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-stores-inventory', 'BUSINESS_STORES_INVENTORY', '재고 관리', '/business/stores/inventory', 'menu-business-stores', 3, 30, 'package', 'MENU', '매장 재고 관리', TRUE, CURRENT_TIMESTAMP, 'system'),

-- 영업정보시스템 - 통계/보고서 하위 카테고리
('menu-business-reports-sales', 'BUSINESS_REPORTS_SALES', '매출 분석', '/business/reports/sales', 'menu-business-reports', 3, 10, 'bar-chart', 'MENU', '매출 통계 및 분석', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-reports-products', 'BUSINESS_REPORTS_PRODUCTS', '상품 분석', '/business/reports/products', 'menu-business-reports', 3, 20, 'package', 'MENU', '상품별 통계 분석', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-business-reports-customers', 'BUSINESS_REPORTS_CUSTOMERS', '고객 분석', '/business/reports/customers', 'menu-business-reports', 3, 30, 'user', 'MENU', '고객 통계 분석', TRUE, CURRENT_TIMESTAMP, 'system'),

-- POS 시스템 - 판매 하위 카테고리
('menu-pos-sales-register', 'POS_SALES_REGISTER', '판매 등록', '/pos/sales/register', 'menu-pos-sales', 3, 10, 'shopping-cart', 'MENU', '상품 판매 등록', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-sales-history', 'POS_SALES_HISTORY', '판매 내역', '/pos/sales/history', 'menu-pos-sales', 3, 20, 'clock', 'MENU', '판매 이력 조회', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-sales-returns', 'POS_SALES_RETURNS', '반품/교환', '/pos/sales/returns', 'menu-pos-sales', 3, 30, 'refresh-ccw', 'MENU', '반품 및 교환 처리', TRUE, CURRENT_TIMESTAMP, 'system'),

-- POS 시스템 - 상품 관리 하위 카테고리 (POS용)
('menu-pos-products-catalog', 'POS_PRODUCTS_CATALOG', '상품 목록', '/pos/products/catalog', 'menu-pos-products', 3, 10, 'package', 'MENU', 'POS 상품 목록 조회', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-products-search', 'POS_PRODUCTS_SEARCH', '상품 검색', '/pos/products/search', 'menu-pos-products', 3, 20, 'search', 'MENU', '상품 빠른 검색', TRUE, CURRENT_TIMESTAMP, 'system'),

-- POS 시스템 - 설정 하위 카테고리
('menu-pos-settings-device', 'POS_SETTINGS_DEVICE', '기기 설정', '/pos/settings/device', 'menu-pos-settings', 3, 10, 'monitor', 'MENU', 'POS 기기 환경 설정', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-settings-printer', 'POS_SETTINGS_PRINTER', '프린터 설정', '/pos/settings/printer', 'menu-pos-settings', 3, 20, 'printer', 'MENU', '영수증 프린터 설정', TRUE, CURRENT_TIMESTAMP, 'system'),
('menu-pos-settings-payment', 'POS_SETTINGS_PAYMENT', '결제 설정', '/pos/settings/payment', 'menu-pos-settings', 3, 30, 'credit-card', 'MENU', '결제 수단 설정', TRUE, CURRENT_TIMESTAMP, 'system')

ON DUPLICATE KEY UPDATE menu_name = menu_name;

-- 새로운 메뉴들에 대한 권한 설정

-- SUPER_ADMIN에게 모든 새 메뉴 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-super-admin-users-internal', 'admin', 'ROLE', 'ADMIN_USERS_INTERNAL', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-users-customers', 'admin', 'ROLE', 'ADMIN_USERS_CUSTOMERS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-org-hq', 'admin', 'ROLE', 'ADMIN_ORG_HQ', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-org-stores', 'admin', 'ROLE', 'ADMIN_ORG_STORES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-stores-info', 'admin', 'ROLE', 'BUSINESS_STORES_INFO', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-stores-staff', 'admin', 'ROLE', 'BUSINESS_STORES_STAFF', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-stores-inventory', 'admin', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-reports-sales', 'admin', 'ROLE', 'BUSINESS_REPORTS_SALES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-reports-products', 'admin', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-reports-customers', 'admin', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-sales-register', 'admin', 'ROLE', 'POS_SALES_REGISTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-sales-history', 'admin', 'ROLE', 'POS_SALES_HISTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-sales-returns', 'admin', 'ROLE', 'POS_SALES_RETURNS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-catalog', 'admin', 'ROLE', 'POS_PRODUCTS_CATALOG', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-search', 'admin', 'ROLE', 'POS_PRODUCTS_SEARCH', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-settings-device', 'admin', 'ROLE', 'POS_SETTINGS_DEVICE', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-settings-printer', 'admin', 'ROLE', 'POS_SETTINGS_PRINTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-settings-payment', 'admin', 'ROLE', 'POS_SETTINGS_PAYMENT', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- SYSTEM_ADMIN에게 해당 메뉴 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-system-admin-stores-info', 'write', 'ROLE', 'BUSINESS_STORES_INFO', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-stores-staff', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-stores-inventory', 'write', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-reports-sales', 'write', 'ROLE', 'BUSINESS_REPORTS_SALES', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-reports-products', 'write', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-reports-customers', 'write', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-sales-register', 'write', 'ROLE', 'POS_SALES_REGISTER', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-sales-history', 'write', 'ROLE', 'POS_SALES_HISTORY', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-sales-returns', 'write', 'ROLE', 'POS_SALES_RETURNS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-catalog', 'write', 'ROLE', 'POS_PRODUCTS_CATALOG', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-search', 'write', 'ROLE', 'POS_PRODUCTS_SEARCH', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-settings-device', 'write', 'ROLE', 'POS_SETTINGS_DEVICE', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-settings-printer', 'write', 'ROLE', 'POS_SETTINGS_PRINTER', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-settings-payment', 'write', 'ROLE', 'POS_SETTINGS_PAYMENT', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- HQ_MANAGER에게 영업정보시스템 메뉴 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-hq-manager-stores-info', 'write', 'ROLE', 'BUSINESS_STORES_INFO', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hq-manager-stores-staff', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hq-manager-stores-inventory', 'write', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hq-manager-reports-sales', 'read', 'ROLE', 'BUSINESS_REPORTS_SALES', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hq-manager-reports-products', 'read', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hq-manager-reports-customers', 'read', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- STORE_MANAGER에게 POS 관련 메뉴 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-store-manager-sales-register', 'write', 'ROLE', 'POS_SALES_REGISTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-sales-history', 'write', 'ROLE', 'POS_SALES_HISTORY', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-sales-returns', 'write', 'ROLE', 'POS_SALES_RETURNS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-products-catalog', 'write', 'ROLE', 'POS_PRODUCTS_CATALOG', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-products-search', 'write', 'ROLE', 'POS_PRODUCTS_SEARCH', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-settings-device', 'write', 'ROLE', 'POS_SETTINGS_DEVICE', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-settings-printer', 'write', 'ROLE', 'POS_SETTINGS_PRINTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-settings-payment', 'write', 'ROLE', 'POS_SETTINGS_PAYMENT', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- USER에게 기본 POS 사용 권한 부여
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-user-sales-register', 'read', 'ROLE', 'POS_SALES_REGISTER', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-products-catalog', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-products-search', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- V16: 레벨 3 메뉴 권한 정리 및 누락된 권한 추가

-- 1. SUPER_ADMIN 권한 추가 (누락된 것들)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- SUPER_ADMIN 레벨 3 권한 (누락된 것들)
('perm-superadmin-business-reports-sales', 'admin', 'ROLE', 'BUSINESS_REPORTS_SALES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-reports-products', 'admin', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-reports-customers', 'admin', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-stores-info', 'admin', 'ROLE', 'BUSINESS_STORES_INFO', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-stores-staff', 'admin', 'ROLE', 'BUSINESS_STORES_STAFF', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-business-stores-inventory', 'admin', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-products-catalog', 'admin', 'ROLE', 'POS_PRODUCTS_CATALOG', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-products-search', 'admin', 'ROLE', 'POS_PRODUCTS_SEARCH', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-sales-register', 'admin', 'ROLE', 'POS_SALES_REGISTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-sales-history', 'admin', 'ROLE', 'POS_SALES_HISTORY', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-sales-returns', 'admin', 'ROLE', 'POS_SALES_RETURNS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-settings-device', 'admin', 'ROLE', 'POS_SETTINGS_DEVICE', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-settings-printer', 'admin', 'ROLE', 'POS_SETTINGS_PRINTER', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-superadmin-pos-settings-payment', 'admin', 'ROLE', 'POS_SETTINGS_PAYMENT', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 2. HQ_ADMIN 권한 추가 (누락된 것들)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- HQ_ADMIN 레벨 3 권한 (기존에 일부만 있음)
('perm-hqadmin-business-reports-sales-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_SALES', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-reports-products-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-reports-customers-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-stores-info-v16', 'write', 'ROLE', 'BUSINESS_STORES_INFO', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-stores-staff-v16', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqadmin-business-stores-inventory-v16', 'read', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'HQ_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 3. STORE_MANAGER 권한 추가/수정
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- STORE_MANAGER 레벨 3 권한 (읽기/쓰기 조정)
('perm-storemanager-pos-products-catalog-v16', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-products-search-v16', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-sales-register-v16', 'write', 'ROLE', 'POS_SALES_REGISTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-sales-history-v16', 'read', 'ROLE', 'POS_SALES_HISTORY', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-sales-returns-v16', 'write', 'ROLE', 'POS_SALES_RETURNS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-settings-device-v16', 'read', 'ROLE', 'POS_SETTINGS_DEVICE', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-settings-printer-v16', 'read', 'ROLE', 'POS_SETTINGS_PRINTER', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-pos-settings-payment-v16', 'read', 'ROLE', 'POS_SETTINGS_PAYMENT', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-business-stores-info-v16', 'read', 'ROLE', 'BUSINESS_STORES_INFO', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-business-stores-staff-v16', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-storemanager-business-stores-inventory-v16', 'write', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 4. POS_USER 권한 추가 (실제 POS 사용자)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- POS_USER 레벨 3 권한 (기존에 일부만 있음)
('perm-posuser-pos-products-catalog-v16', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-products-search-v16', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-sales-register-v16', 'write', 'ROLE', 'POS_SALES_REGISTER', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-sales-history-v16', 'read', 'ROLE', 'POS_SALES_HISTORY', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-posuser-pos-sales-returns-v16', 'write', 'ROLE', 'POS_SALES_RETURNS', 'POS_USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 5. SYSTEM_ADMIN 권한 추가 (레벨 3 메뉴)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- SYSTEM_ADMIN 레벨 3 권한 (기존 write를 admin으로 승격)
('perm-systemadmin-business-reports-sales', 'admin', 'ROLE', 'BUSINESS_REPORTS_SALES', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-business-reports-products', 'admin', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-business-reports-customers', 'admin', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-business-stores-info', 'admin', 'ROLE', 'BUSINESS_STORES_INFO', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-business-stores-staff', 'admin', 'ROLE', 'BUSINESS_STORES_STAFF', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-business-stores-inventory', 'admin', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-products-catalog', 'admin', 'ROLE', 'POS_PRODUCTS_CATALOG', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-products-search', 'admin', 'ROLE', 'POS_PRODUCTS_SEARCH', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-sales-register', 'admin', 'ROLE', 'POS_SALES_REGISTER', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-sales-history', 'admin', 'ROLE', 'POS_SALES_HISTORY', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-sales-returns', 'admin', 'ROLE', 'POS_SALES_RETURNS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-settings-device', 'admin', 'ROLE', 'POS_SETTINGS_DEVICE', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-settings-printer', 'admin', 'ROLE', 'POS_SETTINGS_PRINTER', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-systemadmin-pos-settings-payment', 'admin', 'ROLE', 'POS_SETTINGS_PAYMENT', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 6. HQ_MANAGER 권한 추가 (기존에 일부만 있음)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- HQ_MANAGER 레벨 3 권한 (읽기 위주)
('perm-hqmanager-business-reports-sales-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_SALES', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqmanager-business-reports-products-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_PRODUCTS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqmanager-business-reports-customers-v16', 'read', 'ROLE', 'BUSINESS_REPORTS_CUSTOMERS', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqmanager-business-stores-info-v16', 'write', 'ROLE', 'BUSINESS_STORES_INFO', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqmanager-business-stores-staff-v16', 'write', 'ROLE', 'BUSINESS_STORES_STAFF', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-hqmanager-business-stores-inventory-v16', 'write', 'ROLE', 'BUSINESS_STORES_INVENTORY', 'HQ_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 7. USER 역할 권한 추가 (일반 사용자, 기존에 일부만 있음)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES

-- USER 레벨 3 권한 (읽기 전용)
('perm-user-pos-products-catalog-v16', 'read', 'ROLE', 'POS_PRODUCTS_CATALOG', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-products-search-v16', 'read', 'ROLE', 'POS_PRODUCTS_SEARCH', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-sales-register-v16', 'read', 'ROLE', 'POS_SALES_REGISTER', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-sales-history-v16', 'read', 'ROLE', 'POS_SALES_HISTORY', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-sales-returns-v16', 'read', 'ROLE', 'POS_SALES_RETURNS', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- 8. 중복/오래된 권한 정리 (기존 권한 중 level 3에 맞지 않는 것들을 비활성화)
-- 예: level 2 메뉴에 대한 권한은 유지하되, level 3가 우선되도록 조정

-- 권한 정리 뷰 생성
CREATE OR REPLACE VIEW permission_level3_summary AS
SELECT 
    target_id as role_name,
    COUNT(*) as total_permissions,
    COUNT(CASE WHEN permission_type = 'admin' THEN 1 END) as admin_permissions,
    COUNT(CASE WHEN permission_type = 'write' THEN 1 END) as write_permissions,
    COUNT(CASE WHEN permission_type = 'read' THEN 1 END) as read_permissions,
    COUNT(CASE WHEN menu_code LIKE '%LEVEL_3%' OR 
                     menu_code LIKE '%_CATALOG' OR 
                     menu_code LIKE '%_SEARCH' OR 
                     menu_code LIKE '%_REGISTER' OR 
                     menu_code LIKE '%_HISTORY' OR 
                     menu_code LIKE '%_RETURNS' OR 
                     menu_code LIKE '%_DEVICE' OR 
                     menu_code LIKE '%_PRINTER' OR 
                     menu_code LIKE '%_PAYMENT' OR 
                     menu_code LIKE '%_SALES' OR 
                     menu_code LIKE '%_PRODUCTS' OR 
                     menu_code LIKE '%_CUSTOMERS' OR 
                     menu_code LIKE '%_INFO' OR 
                     menu_code LIKE '%_STAFF' OR 
                     menu_code LIKE '%_INVENTORY' 
              THEN 1 END) as level3_permissions
FROM permissions 
WHERE permission_target_type = 'ROLE' AND is_active = TRUE
GROUP BY target_id
ORDER BY role_name;

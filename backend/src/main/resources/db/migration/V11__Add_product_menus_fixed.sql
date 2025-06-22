-- 상품 관리 메뉴 추가 (V8 수정 버전)

-- 상품 관리 메뉴 추가 (Admin 하위)
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, menu_type, parent_menu_id, icon_name, display_order, is_active, description, created_at, created_by)
VALUES 
('menu-admin-products', 'ADMIN_PRODUCTS', '상품 관리', '/admin/products', 'MENU', 'menu-admin', 'package', 7, TRUE, '매장의 상품을 관리하고 재고를 확인합니다', CURRENT_TIMESTAMP, 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 상품 관리 관련 권한 추가
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
-- 상품 관리 기본 권한 (SUPER_ADMIN)
('perm-super-admin-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-delete', 'delete', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-admin', 'admin', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 시스템 관리자 권한
('perm-system-admin-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-delete', 'delete', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 매장 관리자 권한
('perm-store-manager-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 일반 사용자 (직원) 권한
('perm-user-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- POS 판매 권한 추가 (기존 POS_SALES 메뉴에 추가)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
-- POS 판매 권한
('perm-super-admin-pos-sales-admin', 'admin', 'ROLE', 'POS_SALES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-pos-sales-admin', 'admin', 'ROLE', 'POS_SALES', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-pos-sales-write', 'write', 'ROLE', 'POS_SALES', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-sales-write', 'write', 'ROLE', 'POS_SALES', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

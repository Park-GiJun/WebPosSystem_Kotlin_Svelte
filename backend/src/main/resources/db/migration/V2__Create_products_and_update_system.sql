-- V13: 상품 테이블 생성 및 시스템 업데이트
-- V7-V11 통합 마이그레이션을 깔끔하게 재작성

-- 1. 상품 테이블 생성
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    product_id VARCHAR(36) PRIMARY KEY,
    store_id VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    original_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    category VARCHAR(100) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 0,
    max_stock_level INT NOT NULL DEFAULT 9999,
    barcode VARCHAR(100),
    sku VARCHAR(100),
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    
    CONSTRAINT uk_products_barcode_store UNIQUE (store_id, barcode),
    CONSTRAINT uk_products_sku_store UNIQUE (store_id, sku),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_original_price CHECK (original_price >= 0),
    CONSTRAINT ck_products_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT ck_products_min_stock_level CHECK (min_stock_level >= 0),
    CONSTRAINT ck_products_max_stock_level CHECK (max_stock_level >= min_stock_level),
    CONSTRAINT ck_products_product_type CHECK (product_type IN ('FOOD', 'BEVERAGE', 'DESSERT', 'MERCHANDISE', 'SERVICE', 'ETC')),
    CONSTRAINT ck_products_status CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED', 'TEMPORARILY_UNAVAILABLE', 'PREPARING'))
);

-- 상품 테이블 인덱스
CREATE INDEX idx_products_store_id ON products(store_id);
CREATE INDEX idx_products_category ON products(store_id, category);
CREATE INDEX idx_products_product_type ON products(store_id, product_type);
CREATE INDEX idx_products_status ON products(store_id, status);
CREATE INDEX idx_products_active ON products(store_id, is_active);
CREATE INDEX idx_products_display_order ON products(store_id, display_order);
CREATE INDEX idx_products_name ON products(store_id, name);
CREATE INDEX idx_products_barcode ON products(store_id, barcode);
CREATE INDEX idx_products_sku ON products(store_id, sku);
CREATE INDEX idx_products_deleted ON products(store_id, deleted_at);

-- 3. 상품 관리 메뉴 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, menu_type, parent_menu_id, icon_name, display_order, is_active, description, created_at, created_by)
VALUES 
('menu-admin-products', 'ADMIN_PRODUCTS', '상품 관리', '/admin/products', 'MENU', 'menu-admin', 'package', 7, TRUE, '매장의 상품을 관리하고 재고를 확인합니다', CURRENT_TIMESTAMP, 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 4. 상품 관리 권한 추가
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
('perm-super-admin-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-delete', 'delete', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-super-admin-products-admin', 'admin', 'ROLE', 'ADMIN_PRODUCTS', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-products-delete', 'delete', 'ROLE', 'ADMIN_PRODUCTS', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-products-write', 'write', 'ROLE', 'ADMIN_PRODUCTS', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-products-read', 'read', 'ROLE', 'ADMIN_PRODUCTS', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

-- 5. POS 판매 권한 추가
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at)
VALUES 
('perm-super-admin-pos-sales-admin', 'admin', 'ROLE', 'POS_SALES', 'SUPER_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-system-admin-pos-sales-admin', 'admin', 'ROLE', 'POS_SALES', 'SYSTEM_ADMIN', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-store-manager-pos-sales-write', 'write', 'ROLE', 'POS_SALES', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('perm-user-pos-sales-write', 'write', 'ROLE', 'POS_SALES', 'USER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

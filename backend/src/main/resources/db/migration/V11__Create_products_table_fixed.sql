-- 상품 테이블 생성 (V7에서 실패한 것을 V11에서 재시도)
-- store_id 타입을 stores 테이블과 일치하도록 수정

-- 혹시 남아있을 수 있는 products 테이블 삭제
DROP TABLE IF EXISTS products;

-- 올바른 스키마로 products 테이블 생성
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
    
    -- 외래키 제약조건 (stores 테이블의 store_id와 타입 일치)
    CONSTRAINT fk_products_store_id FOREIGN KEY (store_id) REFERENCES stores(store_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건
    CONSTRAINT uk_products_barcode_store UNIQUE (store_id, barcode),
    CONSTRAINT uk_products_sku_store UNIQUE (store_id, sku),
    
    -- 체크 제약조건
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_original_price CHECK (original_price >= 0),
    CONSTRAINT ck_products_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT ck_products_min_stock_level CHECK (min_stock_level >= 0),
    CONSTRAINT ck_products_max_stock_level CHECK (max_stock_level >= min_stock_level),
    CONSTRAINT ck_products_product_type CHECK (product_type IN ('FOOD', 'BEVERAGE', 'DESSERT', 'MERCHANDISE', 'SERVICE', 'ETC')),
    CONSTRAINT ck_products_status CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED', 'TEMPORARILY_UNAVAILABLE', 'PREPARING'))
);

-- 인덱스 생성
CREATE INDEX idx_products_store_id ON products(store_id);
CREATE INDEX idx_products_category ON products(store_id, category);
CREATE INDEX idx_products_product_type ON products(store_id, product_type);
CREATE INDEX idx_products_status ON products(store_id, status);
CREATE INDEX idx_products_active ON products(store_id, is_active);
CREATE INDEX idx_products_display_order ON products(store_id, display_order);
CREATE INDEX idx_products_name ON products(store_id, name);
CREATE INDEX idx_products_barcode ON products(store_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_sku ON products(store_id, sku) WHERE sku IS NOT NULL;
CREATE INDEX idx_products_low_stock ON products(store_id, stock_quantity, min_stock_level) WHERE is_active = TRUE;
CREATE INDEX idx_products_available_sale ON products(store_id) WHERE is_active = TRUE AND status = 'AVAILABLE' AND stock_quantity > 0 AND deleted_at IS NULL;
CREATE INDEX idx_products_deleted ON products(store_id, deleted_at);

-- 테이블 코멘트
COMMENT ON TABLE products IS '상품 정보 테이블';
COMMENT ON COLUMN products.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN products.store_id IS '매장 ID';
COMMENT ON COLUMN products.name IS '상품명';
COMMENT ON COLUMN products.description IS '상품 설명';
COMMENT ON COLUMN products.price IS '판매가격';
COMMENT ON COLUMN products.original_price IS '원가';
COMMENT ON COLUMN products.category IS '카테고리';
COMMENT ON COLUMN products.product_type IS '상품 유형 (FOOD, BEVERAGE, DESSERT, MERCHANDISE, SERVICE, ETC)';
COMMENT ON COLUMN products.status IS '상품 상태 (AVAILABLE, OUT_OF_STOCK, DISCONTINUED, TEMPORARILY_UNAVAILABLE, PREPARING)';
COMMENT ON COLUMN products.stock_quantity IS '현재 재고 수량';
COMMENT ON COLUMN products.min_stock_level IS '최소 재고 수준';
COMMENT ON COLUMN products.max_stock_level IS '최대 재고 수준';
COMMENT ON COLUMN products.barcode IS '바코드';
COMMENT ON COLUMN products.sku IS 'SKU (Stock Keeping Unit)';
COMMENT ON COLUMN products.image_url IS '상품 이미지 URL';
COMMENT ON COLUMN products.is_active IS '활성화 여부';
COMMENT ON COLUMN products.display_order IS '표시 순서';
COMMENT ON COLUMN products.created_at IS '생성일시';
COMMENT ON COLUMN products.created_by IS '생성자';
COMMENT ON COLUMN products.updated_at IS '수정일시';
COMMENT ON COLUMN products.updated_by IS '수정자';
COMMENT ON COLUMN products.deleted_at IS '삭제일시 (소프트 삭제)';
COMMENT ON COLUMN products.deleted_by IS '삭제자';

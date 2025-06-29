-- V8: stores 테이블에 안전하게 새 컬럼 추가

-- 각 컬럼을 개별적으로 추가하되, 이미 존재하면 무시
-- operation_type 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'operation_type') = 0,
              'ALTER TABLE stores ADD COLUMN operation_type ENUM(''DIRECT'', ''FRANCHISE'') NULL',
              'SELECT "operation_type already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- region_code 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'region_code') = 0,
              'ALTER TABLE stores ADD COLUMN region_code VARCHAR(3) NULL',
              'SELECT "region_code already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- store_number 컬럼  
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'store_number') = 0,
              'ALTER TABLE stores ADD COLUMN store_number VARCHAR(10) NULL',
              'SELECT "store_number already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- business_license 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'business_license') = 0,
              'ALTER TABLE stores ADD COLUMN business_license VARCHAR(50) NULL',
              'SELECT "business_license already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- owner_name 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'owner_name') = 0,
              'ALTER TABLE stores ADD COLUMN owner_name VARCHAR(50) NULL',
              'SELECT "owner_name already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- phone_number 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'phone_number') = 0,
              'ALTER TABLE stores ADD COLUMN phone_number VARCHAR(20) NULL',
              'SELECT "phone_number already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- address 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'address') = 0,
              'ALTER TABLE stores ADD COLUMN address VARCHAR(200) NULL',
              'SELECT "address already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- postal_code 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'postal_code') = 0,
              'ALTER TABLE stores ADD COLUMN postal_code VARCHAR(10) NULL',
              'SELECT "postal_code already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- opening_date 컬럼
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'opening_date') = 0,
              'ALTER TABLE stores ADD COLUMN opening_date DATE NULL',
              'SELECT "opening_date already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- store_type을 ENUM으로 변경 (기존이 VARCHAR인 경우만)
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() AND table_name = 'stores' AND column_name = 'store_type' AND data_type = 'varchar') > 0,
              'ALTER TABLE stores MODIFY COLUMN store_type ENUM(''CHAIN'', ''INDIVIDUAL'') NULL DEFAULT ''INDIVIDUAL''',
              'SELECT "store_type already ENUM"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- store_status에 'CLOSED' 추가
ALTER TABLE stores MODIFY COLUMN store_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED') DEFAULT 'ACTIVE';

-- 매장 관련 권한 추가
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, is_active, created_at, updated_at) VALUES
('perm-store-manager-business-stores', 'read', 'ROLE', 'BUSINESS_STORES', 'STORE_MANAGER', 'system', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE permission_type = permission_type;

-- V14__create_audit_triggers.sql
-- 감사 로그 트리거 생성

-- 기존 audit_logs 테이블 삭제 후 재생성 (더 상세한 구조)
DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    audit_id        VARCHAR(36) NOT NULL UNIQUE DEFAULT (UUID()),
    table_name      VARCHAR(50) NOT NULL,
    record_id       VARCHAR(50) NOT NULL,
    action_type     ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values      JSON NULL,
    new_values      JSON NULL,
    changed_fields  JSON NULL,
    user_id         VARCHAR(36) NULL,
    user_name       VARCHAR(50) NULL,
    ip_address      VARCHAR(45) NULL,
    user_agent      TEXT NULL,
    session_id      VARCHAR(100) NULL,
    request_uri     VARCHAR(500) NULL,
    description     VARCHAR(500) NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_table_name (table_name),
    INDEX idx_record_id (record_id),
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_action_type (action_type),
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_name),
    INDEX idx_created_at (created_at),
    INDEX idx_audit_id (audit_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 공통 감사 로그 함수 생성
DELIMITER $$

CREATE FUNCTION get_changed_fields_json(old_json JSON, new_json JSON) 
RETURNS JSON
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE field_name VARCHAR(255);
    DECLARE old_value JSON;
    DECLARE new_value JSON;
    DECLARE result JSON DEFAULT JSON_OBJECT();
    DECLARE field_cursor CURSOR FOR 
        SELECT DISTINCT k FROM (
            SELECT JSON_UNQUOTE(JSON_EXTRACT(JSON_KEYS(old_json), CONCAT('$[', idx, ']'))) as k
            FROM (SELECT 0 as idx UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t
            WHERE JSON_UNQUOTE(JSON_EXTRACT(JSON_KEYS(old_json), CONCAT('$[', idx, ']'))) IS NOT NULL
            UNION
            SELECT JSON_UNQUOTE(JSON_EXTRACT(JSON_KEYS(new_json), CONCAT('$[', idx, ']'))) as k  
            FROM (SELECT 0 as idx UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t
            WHERE JSON_UNQUOTE(JSON_EXTRACT(JSON_KEYS(new_json), CONCAT('$[', idx, ']'))) IS NOT NULL
        ) fields;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    IF old_json IS NULL OR new_json IS NULL THEN
        RETURN result;
    END IF;
    
    OPEN field_cursor;
    read_loop: LOOP
        FETCH field_cursor INTO field_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        SET old_value = JSON_EXTRACT(old_json, CONCAT('$.', field_name));
        SET new_value = JSON_EXTRACT(new_json, CONCAT('$.', field_name));
        
        IF old_value != new_value OR (old_value IS NULL AND new_value IS NOT NULL) OR (old_value IS NOT NULL AND new_value IS NULL) THEN
            SET result = JSON_SET(result, CONCAT('$.', field_name), JSON_OBJECT('old', old_value, 'new', new_value));
        END IF;
    END LOOP;
    
    CLOSE field_cursor;
    RETURN result;
END$$

DELIMITER ;

-- USERS 테이블 감사 트리거
DELIMITER $$

CREATE TRIGGER audit_users_insert 
    AFTER INSERT ON users 
    FOR EACH ROW 
BEGIN
    INSERT INTO audit_logs (
        table_name, record_id, action_type, new_values, user_id, user_name, description
    ) VALUES (
        'users', 
        NEW.id, 
        'INSERT',
        JSON_OBJECT(
            'id', NEW.id,
            'username', NEW.username,
            'email', NEW.email,
            'roles', NEW.roles,
            'user_status', NEW.user_status,
            'organization_id', NEW.organization_id,
            'organization_type', NEW.organization_type,
            'is_active', NEW.is_active,
            'created_by', NEW.created_by
        ),
        NEW.created_by,
        (SELECT username FROM users WHERE id = NEW.created_by LIMIT 1),
        CONCAT('사용자 생성: ', NEW.username)
    );
END$$

CREATE TRIGGER audit_users_update 
    AFTER UPDATE ON users 
    FOR EACH ROW 
BEGIN
    DECLARE old_json JSON;
    DECLARE new_json JSON;
    DECLARE changed_fields JSON;
    
    SET old_json = JSON_OBJECT(
        'id', OLD.id,
        'username', OLD.username,
        'email', OLD.email,
        'roles', OLD.roles,
        'user_status', OLD.user_status,
        'organization_id', OLD.organization_id,
        'organization_type', OLD.organization_type,
        'is_active', OLD.is_active,
        'updated_by', OLD.updated_by
    );
    
    SET new_json = JSON_OBJECT(
        'id', NEW.id,
        'username', NEW.username,
        'email', NEW.email,
        'roles', NEW.roles,
        'user_status', NEW.user_status,
        'organization_id', NEW.organization_id,
        'organization_type', NEW.organization_type,
        'is_active', NEW.is_active,
        'updated_by', NEW.updated_by
    );
    
    SET changed_fields = get_changed_fields_json(old_json, new_json);
    
    IF JSON_LENGTH(changed_fields) > 0 THEN
        INSERT INTO audit_logs (
            table_name, record_id, action_type, old_values, new_values, changed_fields, 
            user_id, user_name, description
        ) VALUES (
            'users', 
            NEW.id, 
            'UPDATE',
            old_json,
            new_json,
            changed_fields,
            NEW.updated_by,
            (SELECT username FROM users WHERE id = NEW.updated_by LIMIT 1),
            CONCAT('사용자 수정: ', NEW.username)
        );
    END IF;
END$$

CREATE TRIGGER audit_users_delete 
    AFTER UPDATE ON users 
    FOR EACH ROW 
BEGIN
    IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
        INSERT INTO audit_logs (
            table_name, record_id, action_type, old_values, user_id, user_name, description
        ) VALUES (
            'users', 
            NEW.id, 
            'DELETE',
            JSON_OBJECT(
                'id', OLD.id,
                'username', OLD.username,
                'email', OLD.email,
                'deleted_by', NEW.deleted_by,
                'deleted_at', NEW.deleted_at
            ),
            NEW.deleted_by,
            (SELECT username FROM users WHERE id = NEW.deleted_by LIMIT 1),
            CONCAT('사용자 삭제: ', OLD.username)
        );
    END IF;
END$$

DELIMITER ;

-- STORES 테이블 감사 트리거
DELIMITER $$

CREATE TRIGGER audit_stores_insert 
    AFTER INSERT ON stores 
    FOR EACH ROW 
BEGIN
    INSERT INTO audit_logs (
        table_name, record_id, action_type, new_values, user_id, user_name, description
    ) VALUES (
        'stores', 
        NEW.store_id, 
        'INSERT',
        JSON_OBJECT(
            'store_id', NEW.store_id,
            'store_name', NEW.store_name,
            'store_type', NEW.store_type,
            'operation_type', NEW.operation_type,
            'hq_id', NEW.hq_id,
            'region_code', NEW.region_code,
            'owner_name', NEW.owner_name,
            'business_license', NEW.business_license,
            'store_status', NEW.store_status,
            'is_active', NEW.is_active,
            'created_by', NEW.created_by
        ),
        NEW.created_by,
        (SELECT username FROM users WHERE id = NEW.created_by LIMIT 1),
        CONCAT('매장 생성: ', NEW.store_name)
    );
END$$

CREATE TRIGGER audit_stores_update 
    AFTER UPDATE ON stores 
    FOR EACH ROW 
BEGIN
    DECLARE old_json JSON;
    DECLARE new_json JSON;
    DECLARE changed_fields JSON;
    
    SET old_json = JSON_OBJECT(
        'store_id', OLD.store_id,
        'store_name', OLD.store_name,
        'store_type', OLD.store_type,
        'operation_type', OLD.operation_type,
        'hq_id', OLD.hq_id,
        'region_code', OLD.region_code,
        'owner_name', OLD.owner_name,
        'business_license', OLD.business_license,
        'store_status', OLD.store_status,
        'is_active', OLD.is_active,
        'updated_by', OLD.updated_by
    );
    
    SET new_json = JSON_OBJECT(
        'store_id', NEW.store_id,
        'store_name', NEW.store_name,
        'store_type', NEW.store_type,
        'operation_type', NEW.operation_type,
        'hq_id', NEW.hq_id,
        'region_code', NEW.region_code,
        'owner_name', NEW.owner_name,
        'business_license', NEW.business_license,
        'store_status', NEW.store_status,
        'is_active', NEW.is_active,
        'updated_by', NEW.updated_by
    );
    
    SET changed_fields = get_changed_fields_json(old_json, new_json);
    
    IF JSON_LENGTH(changed_fields) > 0 THEN
        INSERT INTO audit_logs (
            table_name, record_id, action_type, old_values, new_values, changed_fields,
            user_id, user_name, description
        ) VALUES (
            'stores', 
            NEW.store_id, 
            'UPDATE',
            old_json,
            new_json,
            changed_fields,
            NEW.updated_by,
            (SELECT username FROM users WHERE id = NEW.updated_by LIMIT 1),
            CONCAT('매장 수정: ', NEW.store_name)
        );
    END IF;
END$$

DELIMITER ;

-- PERMISSIONS 테이블 감사 트리거
DELIMITER $$

CREATE TRIGGER audit_permissions_insert 
    AFTER INSERT ON permissions 
    FOR EACH ROW 
BEGIN
    INSERT INTO audit_logs (
        table_name, record_id, action_type, new_values, user_id, user_name, description
    ) VALUES (
        'permissions', 
        NEW.id, 
        'INSERT',
        JSON_OBJECT(
            'id', NEW.id,
            'permission_type', NEW.permission_type,
            'permission_target_type', NEW.permission_target_type,
            'menu_code', NEW.menu_code,
            'target_id', NEW.target_id,
            'granted_by', NEW.granted_by,
            'is_active', NEW.is_active
        ),
        NEW.granted_by,
        (SELECT username FROM users WHERE id = NEW.granted_by LIMIT 1),
        CONCAT('권한 부여: ', NEW.menu_code, ' -> ', NEW.target_id)
    );
END$$

CREATE TRIGGER audit_permissions_update 
    AFTER UPDATE ON permissions 
    FOR EACH ROW 
BEGIN
    DECLARE old_json JSON;
    DECLARE new_json JSON;
    DECLARE changed_fields JSON;
    
    SET old_json = JSON_OBJECT(
        'id', OLD.id,
        'permission_type', OLD.permission_type,
        'permission_target_type', OLD.permission_target_type,
        'menu_code', OLD.menu_code,
        'target_id', OLD.target_id,
        'is_active', OLD.is_active
    );
    
    SET new_json = JSON_OBJECT(
        'id', NEW.id,
        'permission_type', NEW.permission_type,
        'permission_target_type', NEW.permission_target_type,
        'menu_code', NEW.menu_code,
        'target_id', NEW.target_id,
        'is_active', NEW.is_active
    );
    
    SET changed_fields = get_changed_fields_json(old_json, new_json);
    
    IF JSON_LENGTH(changed_fields) > 0 THEN
        INSERT INTO audit_logs (
            table_name, record_id, action_type, old_values, new_values, changed_fields,
            user_id, user_name, description
        ) VALUES (
            'permissions', 
            NEW.id, 
            'UPDATE',
            old_json,
            new_json,
            changed_fields,
            'system',
            'system',
            CONCAT('권한 수정: ', NEW.menu_code, ' -> ', NEW.target_id)
        );
    END IF;
END$$

DELIMITER ;

-- 테스트용 감사 로그 데이터 추가
INSERT INTO audit_logs (
    table_name, record_id, action_type, new_values, user_id, user_name, 
    ip_address, description, created_at
) VALUES 
('users', 'admin', 'INSERT', 
 JSON_OBJECT('username', 'admin', 'email', 'admin@example.com', 'roles', 'ADMIN'),
 'system', 'system', '127.0.0.1', '시스템 관리자 계정 생성', 
 DATE_SUB(NOW(), INTERVAL 30 DAY)),

('users', 'store_admin_001', 'INSERT',
 JSON_OBJECT('username', 'gangnam_store', 'email', 'gangnam@example.com', 'roles', 'STORE_ADMIN'),
 'admin', 'admin', '192.168.1.100', '강남 편의점 관리자 계정 생성',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

('stores', 'IN001001', 'INSERT',
 JSON_OBJECT('store_name', '강남 편의점', 'store_type', 'INDIVIDUAL', 'owner_name', '김편의'),
 'admin', 'admin', '192.168.1.100', '개인매장 생성: 강남 편의점',
 DATE_SUB(NOW(), INTERVAL 15 DAY)),

('stores', 'IN001001', 'UPDATE',
 JSON_OBJECT('store_status', 'ACTIVE', 'phone_number', '02-1234-5678'),
 JSON_OBJECT('store_status', 'ACTIVE', 'phone_number', '02-1234-9999'),
 JSON_OBJECT('phone_number', JSON_OBJECT('old', '02-1234-5678', 'new', '02-1234-9999')),
 'store_admin_001', 'gangnam_store', '192.168.1.101', '매장 전화번호 수정',
 DATE_SUB(NOW(), INTERVAL 5 DAY)),

('permissions', 'perm_user_001', 'INSERT',
 JSON_OBJECT('menu_code', 'POS_SALES', 'target_id', 'store_admin_001', 'permission_type', 'ADMIN'),
 'admin', 'admin', '192.168.1.100', 'POS 판매 권한 부여',
 DATE_SUB(NOW(), INTERVAL 10 DAY));

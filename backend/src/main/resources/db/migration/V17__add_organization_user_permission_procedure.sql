-- 본사 생성시 자동으로 관리자 권한 부여하는 프로시저
DROP PROCEDURE IF EXISTS grant_headquarters_admin_permissions;
CREATE PROCEDURE grant_headquarters_admin_permissions(
    IN p_hq_id VARCHAR(50),
    IN p_admin_user_id VARCHAR(50)
)
BEGIN
    DECLARE now_time DATETIME;
    SET now_time = NOW();
    
    -- 1. 본사 관리자에게 영업정보시스템의 모든 메뉴 권한 부여
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    SELECT 
        UUID(),
        'admin',
        'USER',
        m.menu_code,
        p_admin_user_id,
        'system',
        now_time,
        1
    FROM menus m
    WHERE m.parent_menu_id = 'menu-business'  -- 영업정보시스템 하위 메뉴들
       OR m.menu_id = 'menu-business';       -- 영업정보시스템 자체
    
    -- 2. 본사 조직 자체에도 영업정보시스템 권한 부여
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    SELECT 
        UUID(),
        'admin',
        'ORGANIZATION',
        m.menu_code,
        p_hq_id,
        'system',
        now_time,
        1
    FROM menus m
    WHERE m.parent_menu_id = 'menu-business'
       OR m.menu_id = 'menu-business';
       
    -- 3. 본사 관리자에게 사용자 생성 권한 부여 (본사 계정 생성용)
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    VALUES (UUID(), 'write', 'USER', 'ADMIN_USERS', p_admin_user_id, 'system', now_time, 1);
    
    -- 4. 본사에게 사용자 생성 권한 부여 (조직 권한)
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    VALUES (UUID(), 'write', 'ORGANIZATION', 'ADMIN_USERS', p_hq_id, 'system', now_time, 1);
END;

-- 매장 생성시 자동으로 관리자 권한 부여하는 프로시저
DROP PROCEDURE IF EXISTS grant_store_admin_permissions;
CREATE PROCEDURE grant_store_admin_permissions(
    IN p_store_id VARCHAR(50),
    IN p_admin_user_id VARCHAR(50),
    IN p_is_chain BOOLEAN
)
BEGIN
    DECLARE now_time DATETIME;
    SET now_time = NOW();
    
    -- 1. 매장 관리자에게 POS 시스템의 모든 메뉴 권한 부여
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    SELECT 
        UUID(),
        'admin',
        'USER',
        m.menu_code,
        p_admin_user_id,
        'system',
        now_time,
        1
    FROM menus m
    WHERE m.parent_menu_id = 'menu-pos'  -- POS 시스템 하위 메뉴들
       OR m.menu_id = 'menu-pos';         -- POS 시스템 자체
    
    -- 2. 매장 조직 자체에도 POS 시스템 권한 부여
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    SELECT 
        UUID(),
        'admin',
        'ORGANIZATION',
        m.menu_code,
        p_store_id,
        'system',
        now_time,
        1
    FROM menus m
    WHERE m.parent_menu_id = 'menu-pos'
       OR m.menu_id = 'menu-pos';
       
    -- 3. 매장 관리자에게 사용자 생성 권한 부여 (매장 계정 생성용)
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    VALUES (UUID(), 'write', 'USER', 'ADMIN_USERS', p_admin_user_id, 'system', now_time, 1);
    
    -- 4. 매장에게 사용자 생성 권한 부여 (조직 권한)
    INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
    VALUES (UUID(), 'write', 'ORGANIZATION', 'ADMIN_USERS', p_store_id, 'system', now_time, 1);
    
    -- 5. 개인매장인 경우 영업정보시스템의 자신 매장 관련 권한도 부여
    IF NOT p_is_chain THEN
        -- 매장 관리 메뉴에 대한 읽기 권한 (자신의 매장 정보만 볼 수 있도록)
        INSERT IGNORE INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by, created_at, is_active)
        VALUES 
            (UUID(), 'read', 'USER', 'BUSINESS_STORES', p_admin_user_id, 'system', now_time, 1),
            (UUID(), 'read', 'USER', 'BUSINESS_REPORTS', p_admin_user_id, 'system', now_time, 1);
    END IF;
END;

-- 테스트 데이터 업데이트 - 기존 관리자에게 권한 부여
-- 본사 관리자 권한
CALL grant_headquarters_admin_permissions('HQSTT', 'admin-user-id');
CALL grant_headquarters_admin_permissions('HQTET', 'admin-user-id');

-- V10__add_cache_management_menu.sql
-- 캐시 관리 메뉴 및 권한 추가

-- 캐시 관리 메뉴 추가
INSERT INTO menu (
    menu_id, menu_code, menu_name, menu_path, parent_menu_id, 
    menu_level, display_order, icon_name, menu_type, 
    is_active, created_at, updated_at
) VALUES (
    'menu-cache-management', 'CACHE_MANAGEMENT', '캐시 관리', '/admin/cache',
    'menu-system-admin', 3, 4, 'database',
    'MENU', true, NOW(), NOW()
);

-- SUPER_ADMIN 역할에 캐시 관리 메뉴의 ADMIN 권한 부여
INSERT INTO permission (
    permission_id, menu_id, target_type, target_id, permission_type,
    granted_by, granted_at, expires_at, is_active, created_at, updated_at
) VALUES (
    CONCAT('perm-', UUID()), 'menu-cache-management', 'ROLE', 'SUPER_ADMIN', 'ADMIN',
    'SYSTEM', NOW(), NULL, true, NOW(), NOW()
);

-- SYSTEM_ADMIN 역할에 캐시 관리 메뉴의 ADMIN 권한 부여
INSERT INTO permission (
    permission_id, menu_id, target_type, target_id, permission_type,
    granted_by, granted_at, expires_at, is_active, created_at, updated_at
) VALUES (
    CONCAT('perm-', UUID()), 'menu-cache-management', 'ROLE', 'SYSTEM_ADMIN', 'ADMIN',
    'SYSTEM', NOW(), NULL, true, NOW(), NOW()
);

-- 조직 권한 관리 메뉴 추가
INSERT INTO menu (
    menu_id, menu_code, menu_name, menu_path, parent_menu_id, 
    menu_level, display_order, icon_name, menu_type, 
    is_active, created_at, updated_at
) VALUES (
    'menu-organization-permissions', 'ORGANIZATION_PERMISSIONS', '조직 권한 관리', '/admin/organization-permissions',
    'menu-system-admin', 3, 5, 'building',
    'MENU', true, NOW(), NOW()
);

-- SUPER_ADMIN 역할에 조직 권한 관리 메뉴의 ADMIN 권한 부여
INSERT INTO permission (
    permission_id, menu_id, target_type, target_id, permission_type,
    granted_by, granted_at, expires_at, is_active, created_at, updated_at
) VALUES (
    CONCAT('perm-', UUID()), 'menu-organization-permissions', 'ROLE', 'SUPER_ADMIN', 'ADMIN',
    'SYSTEM', NOW(), NULL, true, NOW(), NOW()
);

-- SYSTEM_ADMIN 역할에 조직 권한 관리 메뉴의 ADMIN 권한 부여
INSERT INTO permission (
    permission_id, menu_id, target_type, target_id, permission_type,
    granted_by, granted_at, expires_at, is_active, created_at, updated_at
) VALUES (
    CONCAT('perm-', UUID()), 'menu-organization-permissions', 'ROLE', 'SYSTEM_ADMIN', 'ADMIN',
    'SYSTEM', NOW(), NULL, true, NOW(), NOW()
);

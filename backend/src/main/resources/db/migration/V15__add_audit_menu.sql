-- V15__add_audit_menu.sql
-- 감사 로그 메뉴 추가

-- 감사 로그 메뉴 추가
INSERT INTO menus (menu_id, menu_code, menu_name, menu_path, parent_menu_id, menu_level, display_order, icon_name, menu_type, created_by)
VALUES ('menu-admin-audit', 'ADMIN_AUDIT', '감사 로그', '/admin/audit', 'menu-admin', 2, 50, 'document-magnifying-glass', 'MENU', 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 감사 로그 메뉴 권한 추가 (ADMIN 역할에게만)
INSERT INTO permissions (id, permission_type, permission_target_type, menu_code, target_id, granted_by)
VALUES ('perm_admin_audit_read', 'READ', 'ROLE', 'ADMIN_AUDIT', 'ADMIN', 'system')
ON DUPLICATE KEY UPDATE permission_type = VALUES(permission_type);

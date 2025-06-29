-- V4: 관리자 페이지에서 상품관리 메뉴 삭제

-- 관리자 상품관리 메뉴 삭제
DELETE FROM permissions WHERE menu_code = 'ADMIN_PRODUCTS';
DELETE FROM menus WHERE menu_code = 'ADMIN_PRODUCTS';

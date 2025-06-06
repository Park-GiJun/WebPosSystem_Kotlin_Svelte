-- V2: 기본 관리자 사용자 생성

-- 관리자 사용자 삽입 (BCrypt로 암호화된 'password123')
INSERT INTO users (id, username, email, password_hash, roles, user_status, email_verified_at, is_active, created_at, updated_at, version)
VALUES (
    'admin-user-id',
    'admin',
    'admin@webpos.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- password123
    '["SUPER_ADMIN"]',
    'ACTIVE',
    NOW(),
    true,
    NOW(),
    NOW(),
    0
);

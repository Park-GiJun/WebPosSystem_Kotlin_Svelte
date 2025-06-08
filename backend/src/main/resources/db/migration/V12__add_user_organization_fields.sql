-- V12__add_user_organization_fields.sql
-- 사용자 테이블에 조직 관련 필드 추가

-- users 테이블에 organization 관련 컬럼 추가
ALTER TABLE users 
ADD COLUMN organization_id VARCHAR(50) NULL COMMENT '소속 조직 ID (본사 또는 매장)',
ADD COLUMN organization_type VARCHAR(20) NULL COMMENT '조직 유형 (HEADQUARTERS, INDIVIDUAL_STORE)',
ADD INDEX idx_organization (organization_id, organization_type);

-- 기존 admin 사용자 업데이트 (시스템 관리자는 조직에 소속되지 않음)
UPDATE users 
SET organization_type = 'SYSTEM' 
WHERE username = 'admin' AND organization_id IS NULL;

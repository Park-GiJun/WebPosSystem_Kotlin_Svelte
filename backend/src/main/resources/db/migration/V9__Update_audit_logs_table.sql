-- audit_logs 테이블에 누락된 컬럼 추가

-- audit_id 컬럼 추가 (UUID)
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS audit_id VARCHAR(36);

-- session_id 컬럼 추가
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS session_id VARCHAR(255);

-- request_uri 컬럼 추가
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS request_uri VARCHAR(500);

-- 기존 데이터에 대해 audit_id 생성 (NULL인 경우에만)
UPDATE audit_logs 
SET audit_id = CONCAT('audit-', id, '-', EXTRACT(EPOCH FROM created_at)::BIGINT)
WHERE audit_id IS NULL;

-- audit_id를 NOT NULL로 변경
ALTER TABLE audit_logs ALTER COLUMN audit_id SET NOT NULL;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_audit_logs_audit_id ON audit_logs(audit_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_session_id ON audit_logs(session_id) WHERE session_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_audit_logs_request_uri ON audit_logs(request_uri) WHERE request_uri IS NOT NULL;

-- 컬럼 코멘트 추가
COMMENT ON COLUMN audit_logs.audit_id IS '감사 로그 고유 식별자 (UUID)';
COMMENT ON COLUMN audit_logs.session_id IS '사용자 세션 ID';
COMMENT ON COLUMN audit_logs.request_uri IS '요청 URI';

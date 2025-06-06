-- V3: 외래키 제약조건 수정 - created_by를 nullable로 변경

-- users 테이블의 created_by 외래키 제약조건을 일시적으로 제거
ALTER TABLE users DROP FOREIGN KEY users_ibfk_1;

-- 새로운 외래키 제약조건 추가 (NULL 허용)
ALTER TABLE users 
ADD CONSTRAINT users_created_by_fk 
FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

-- updated_by와 deleted_by에 대한 외래키 제약조건도 추가
ALTER TABLE users 
ADD CONSTRAINT users_updated_by_fk 
FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE users 
ADD CONSTRAINT users_deleted_by_fk 
FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL;

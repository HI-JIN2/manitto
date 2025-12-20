-- V2__add_user_to_participant.sql
-- Participant 테이블에 user_id 추가 및 데이터 마이그레이션

-- 1. user_id 컬럼 추가 (nullable로 먼저 추가)
ALTER TABLE participant ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 2. nickname 컬럼 추가
ALTER TABLE participant ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);

-- 3. 기존 데이터 마이그레이션: email 기반으로 user와 연결
-- 기존 participant의 email과 일치하는 user가 있으면 연결
UPDATE participant p
SET user_id = u.id
FROM users u
WHERE p.email = u.email
  AND p.user_id IS NULL;

-- 4. user가 없는 participant를 위해 user 생성
INSERT INTO users (email)
SELECT DISTINCT p.email
FROM participant p
WHERE p.user_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.email = p.email);

-- 5. 다시 user_id 연결 (새로 생성된 user 포함)
UPDATE participant p
SET user_id = u.id
FROM users u
WHERE p.email = u.email
  AND p.user_id IS NULL;

-- 6. user_id를 NOT NULL로 변경
ALTER TABLE participant ALTER COLUMN user_id SET NOT NULL;

-- 7. Foreign Key 추가
ALTER TABLE participant 
ADD CONSTRAINT fk_participant_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- 8. email 컬럼 제거
ALTER TABLE participant DROP COLUMN IF EXISTS email;


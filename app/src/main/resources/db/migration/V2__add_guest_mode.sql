-- V2__add_guest_mode.sql
-- 게스트 모드 지원을 위한 스키마 변경

-- Participant 테이블에 게스트 필드 추가
ALTER TABLE participant 
    ALTER COLUMN user_id DROP NOT NULL,
    ADD COLUMN guest_name VARCHAR(255),
    ADD COLUMN guest_email VARCHAR(255);

-- Party 테이블의 host_id를 nullable로 변경 (게스트 모드)
ALTER TABLE party 
    ALTER COLUMN host_id DROP NOT NULL;

-- 게스트 모드 참가자도 고유하도록 제약 조건 수정
-- (user_id가 null일 때는 guest_email로 중복 체크)
DROP INDEX IF EXISTS idx_participant_user_id;
CREATE INDEX idx_participant_user_id ON participant(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_participant_guest_email ON participant(guest_email) WHERE guest_email IS NOT NULL;

-- 게스트 모드 참가자 중복 방지 (같은 파티에 같은 이메일로 중복 참가 방지)
CREATE UNIQUE INDEX idx_participant_party_guest_email ON participant(party_id, guest_email) 
    WHERE guest_email IS NOT NULL AND user_id IS NULL;


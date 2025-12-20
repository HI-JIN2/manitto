-- V3__fix_host_id_nullable.sql
-- 게스트 모드 지원: host_id를 nullable로 변경

-- host_id를 nullable로 변경
ALTER TABLE party ALTER COLUMN host_id DROP NOT NULL;


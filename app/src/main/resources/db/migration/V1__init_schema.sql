-- V1__init_schema.sql
-- 초기 스키마 생성

-- Users 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    picture VARCHAR(500)
);

-- Party 테이블
CREATE TABLE party (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    invite_code VARCHAR(6) NOT NULL UNIQUE,
    host_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Participant 테이블
CREATE TABLE participant (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    party_id BIGINT NOT NULL REFERENCES party(id),
    nickname VARCHAR(255),
    UNIQUE(user_id, party_id)
);

-- MatchedResult 테이블
CREATE TABLE matched_result (
    id BIGSERIAL PRIMARY KEY,
    giver_id BIGINT NOT NULL REFERENCES participant(id),
    receiver_id BIGINT NOT NULL REFERENCES participant(id),
    party_id BIGINT NOT NULL REFERENCES party(id)
);

-- 인덱스
CREATE INDEX idx_participant_party_id ON participant(party_id);
CREATE INDEX idx_participant_user_id ON participant(user_id);
CREATE INDEX idx_matched_result_party_id ON matched_result(party_id);
CREATE INDEX idx_party_invite_code ON party(invite_code);


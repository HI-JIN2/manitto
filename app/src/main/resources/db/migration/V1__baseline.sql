-- V1__baseline.sql
-- 기존 테이블 구조 (baseline) - 이미 테이블이 있다면 Flyway baseline 명령 실행 필요

-- Users 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    picture VARCHAR(500)
);

-- Party 테이블
CREATE TABLE IF NOT EXISTS party (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Participant 테이블 (기존 구조)
CREATE TABLE IF NOT EXISTS participant (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    party_id BIGINT REFERENCES party(id)
);

-- MatchedResult 테이블
CREATE TABLE IF NOT EXISTS matched_result (
    id BIGSERIAL PRIMARY KEY,
    giver_id BIGINT REFERENCES participant(id),
    receiver_id BIGINT REFERENCES participant(id),
    party_id BIGINT REFERENCES party(id)
);


-- V4__add_email_job_queue.sql
-- 매칭 이메일 발송을 위한 작업 큐 테이블

CREATE TABLE email_job (
    id BIGSERIAL PRIMARY KEY,
    matched_result_id BIGINT NOT NULL REFERENCES matched_result(id) ON DELETE CASCADE,
    party_id BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
    to_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, IN_PROGRESS, RETRY, SUCCESS, FAILED
    attempt_count INT NOT NULL DEFAULT 0,
    next_run_at TIMESTAMP NOT NULL DEFAULT NOW(),
    locked_at TIMESTAMP,
    locked_by VARCHAR(100),
    sent_at TIMESTAMP,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_email_job_matched_result_id ON email_job(matched_result_id);
CREATE INDEX idx_email_job_party_id ON email_job(party_id);
CREATE INDEX idx_email_job_status_next_run_at ON email_job(status, next_run_at);

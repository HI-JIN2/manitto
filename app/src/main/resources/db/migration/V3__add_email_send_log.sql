-- V3__add_email_send_log.sql
-- 이메일 발송 이력 테이블 생성 (멱등성 보장)

CREATE TABLE email_send_log (
    task_id VARCHAR(255) PRIMARY KEY,
    to_email VARCHAR(255) NOT NULL,
    party_id BIGINT,
    status VARCHAR(20) NOT NULL,  -- SUCCESS, FAILED, PENDING
    sent_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_send_log_party_id ON email_send_log(party_id);
CREATE INDEX idx_email_send_log_status ON email_send_log(status);
CREATE INDEX idx_email_send_log_created_at ON email_send_log(created_at);


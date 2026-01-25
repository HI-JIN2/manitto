# 🔍 트러블슈팅 (Troubleshooting)

### 애플리케이션이 시작되지 않음

**증상**: `Started ManittoApplicationKt` 로그가 없음

**해결 방법:**

1. 로그 확인: `docker logs manitto-backend --tail 100`
2. `prod` 프로필 확인: 로그에 `The following 1 profile is active: "prod"` 확인
3. DB 연결 확인: `HikariPool-1 - Start completed` 확인
4. Flyway 마이그레이션 확인: `matched_result` 테이블 존재 여부 확인

### 스키마 검증 실패

**에러**: `Schema-validation: missing table [matched_result]`

**해결 방법:**

```sql
-- DB에 직접 접속하여 테이블 생성
CREATE TABLE IF NOT EXISTS matched_result (
    id BIGSERIAL PRIMARY KEY,
    giver_id BIGINT NOT NULL REFERENCES participant(id),
    receiver_id BIGINT NOT NULL REFERENCES participant(id),
    party_id BIGINT NOT NULL REFERENCES party(id)
);
CREATE INDEX IF NOT EXISTS idx_matched_result_party_id ON matched_result(party_id);
```

### Swagger UI 접근 불가

**확인 사항:**

- `SecurityConfig`에서 Swagger 경로가 `permitAll()`로 설정되어 있는지 확인
- URL: `http://localhost:8080/swagger-ui/index.html` 또는 `http://<YOUR_SERVER_IP>:8080/swagger-ui/index.html`

### 이메일 발송 실패

**확인 사항:**

- Gmail 앱 비밀번호 사용 (일반 비밀번호 아님)
- `MAIL_USERNAME`, `MAIL_PASSWORD` 환경변수 확인
- SMTP 포트: 587 (TLS)

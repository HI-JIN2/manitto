# 🚀 Manitto Backend 배포 가이드 (NCP + Docker)

## 📋 사전 준비

### 1. NCP 서버 생성
- **OS**: Ubuntu 22.04 LTS 권장
- **스펙**: 최소 2vCPU, 4GB RAM
- **포트 오픈 (ACG)**: 8080

### 2. Docker 설치

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo apt-get update
sudo apt-get install docker-compose-plugin

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 재로그인
exit
```

---

## 🔧 배포

### 1. 클론

```bash
git clone <backend-repo-url> manitto-backend
cd manitto-backend
```

### 2. 환경변수 설정

```bash
cp env.example .env
nano .env
```

**.env 설정:**
```env
# 데이터베이스
DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/manitto
DB_USER=postgres
DB_PASSWORD=your_secure_db_password

# JWT
JWT_SECRET=생성된_시크릿_키        # openssl rand -hex 32

# OAuth
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
KAKAO_REST_API_KEY=your_kakao_rest_api_key

# 이메일
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password    # Gmail 앱 비밀번호

# JPA
DDL_AUTO=validate
SHOW_SQL=false

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# 애플리케이션
APP_BASE_URL=https://api.your-domain.com
APP_CORS_ALLOWED_ORIGINS=https://your-frontend.com
```

### 3. 실행

```bash
docker compose up -d --build
```

### 4. 확인

```bash
docker compose ps
curl http://localhost:8080/swagger-ui.html
```

---

## ✅ 접속 URL

| 서비스 | URL |
|--------|-----|
| Backend API | `http://YOUR_NCP_IP:8080` |
| Swagger | `http://YOUR_NCP_IP:8080/swagger-ui.html` |

---

## 🔄 업데이트

### CI/CD 자동 배포

`master` 브랜치에 푸시하면 GitHub Actions가 자동으로 배포합니다.

```bash
git add .
git commit -m "your commit message"
git push origin master
```

### 수동 업데이트

```bash
cd /opt/manitto-backend
git pull origin master
docker-compose down
docker-compose up -d --build
docker image prune -f
docker logs manitto-backend -f
```

---

## 🛠 유용한 명령어

```bash
# 로그 확인
docker compose logs -f backend
docker compose logs -f db

# 재시작
docker compose restart backend

# 중지
docker compose down

# 데이터 포함 삭제 (주의!)
docker compose down -v
```

---

## 🔒 보안 체크리스트

- [ ] NCP ACG에서 5432 포트 차단 (DB 외부 접근 방지)
- [ ] 강력한 DB 비밀번호 사용
- [ ] 새로운 JWT_SECRET 생성 (`openssl rand -hex 32`)
- [ ] 운영환경에서 `DDL_AUTO=validate`
- [ ] `SPRING_PROFILES_ACTIVE=prod` 설정 확인
- [ ] Gmail 앱 비밀번호 사용 (일반 비밀번호 아님)
- [ ] `.env` 파일 권한 설정 (`chmod 600 .env`)

## 🐛 트러블슈팅

### 애플리케이션이 시작되지 않음

**로그 확인:**

```bash
docker logs manitto-backend --tail 100
```

**확인 사항:**

- `prod` 프로필 적용 여부: `The following 1 profile is active: "prod"`
- DB 연결: `HikariPool-1 - Start completed`
- Flyway 마이그레이션: `matched_result` 테이블 존재 여부

**스키마 검증 실패 시:**

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

### Swagger 접근 불가

- URL 확인: `http://your-server:8080/swagger-ui.html`
- `SecurityConfig`에서 Swagger 경로가 `permitAll()`로 설정되어 있는지 확인

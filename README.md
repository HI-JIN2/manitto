# Manitto Backend

Spring Boot + Kotlin 기반 마니또 서비스 백엔드

## 🛠 기술 스택

- Kotlin + Spring Boot 3.3
- PostgreSQL
- JWT Authentication
- Google OAuth 2.0

## 🚀 배포 (Docker)

### 1. 환경변수 설정

```bash
cp env.example .env
nano .env
```

### 2. 실행

```bash
docker compose up -d --build
```

### 3. 확인

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

## 🔧 로컬 개발

```bash
# PostgreSQL 실행 필요
./gradlew :app:bootRun
```

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/party` | 파티 생성 |
| GET | `/api/party/{code}` | 파티 조회 |
| POST | `/api/party/{code}/join` | 파티 참가 |
| POST | `/api/party/{code}/match` | 마니또 매칭 |


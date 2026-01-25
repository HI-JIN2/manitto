# Manitto Backend

Spring Boot + Kotlin 기반 마니또(Secret Santa) 서비스 백엔드 API

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=flat&logo=springboot&logoColor=white)
![Build](https://img.shields.io/badge/Build-Gradle-02303A?style=flat&logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat)

## 📋 목차

- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [환경 설정](#-환경-설정)
- [로컬 개발](#-로컬-개발)
- [API 엔드포인트](#-api-엔드포인트)
- [배포 (Deployment)](#-배포-deployment)
- [트러블슈팅](#-트러블슈팅)

## 🛠 기술 스택

- **언어**: Kotlin 1.9.23
- **프레임워크**: Spring Boot 3.3.0
- **데이터베이스**: PostgreSQL (NCP Cloud DB)
- **인증**: JWT, Google OAuth 2.0
- **문서화**: SpringDoc OpenAPI (Swagger UI)
- **마이그레이션**: Flyway
- **이메일**: JavaMailSender (Gmail SMTP)
- **빌드 도구**: Gradle 8.5
- **컨테이너**: Docker, Docker Compose

## 📁 프로젝트 구조

```
backend/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/party/manitto/
│   │   │   │   ├── config/          # 설정 클래스
│   │   │   │   ├── domain/         # 도메인별 비즈니스 로직
│   │   │   │   │   ├── party/      # 파티 관리
│   │   │   │   │   ├── participant/# 참가자 관리
│   │   │   │   │   ├── match/      # 매칭 로직
│   │   │   │   │   └── user/       # 사용자 인증
│   │   │   │   └── global/         # 공통 유틸리티
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/   # Flyway 마이그레이션
│   │   └── test/
│   └── build.gradle.kts
├── docker-compose.yml
├── Dockerfile
├── .github/workflows/deploy.yml
└── README.md
```

## ⚙️ 환경 설정

### 필수 환경변수

`.env` 파일을 생성하고 다음 변수들을 설정하세요:

```bash
# 데이터베이스 (NCP Cloud DB)
DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/manitto
DB_USER=postgres
DB_PASSWORD=your_rds_password

# JWT (32자 이상)
JWT_SECRET=your_jwt_secret_key_32chars_minimum

# Google OAuth
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com

# Kakao OAuth (선택)
KAKAO_REST_API_KEY=your_kakao_rest_api_key

# Gmail SMTP
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# JPA 설정
DDL_AUTO=validate          # 운영: validate, 개발: update
SHOW_SQL=false

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 설정
APP_BASE_URL=http://manito-party.online:8080
FRONTEND_BASE_URL=https://manitto-frontend.vercel.app
APP_DOMAIN=manito-party.online
APP_CORS_ALLOWED_ORIGINS=https://manitto-frontend.vercel.app,http://localhost:3000,http://localhost:3002
```

### 환경변수 생성

```bash
# JWT Secret 생성
openssl rand -hex 32

# .env 파일 복사
cp env.example .env
nano .env
```

## 🔧 로컬 개발

### 사전 요구사항

- JDK 17 이상
- PostgreSQL 14 이상
- Docker & Docker Compose (선택)

### 로컬 실행

#### 방법 1: Gradle로 직접 실행

```bash
# PostgreSQL 실행 필요 (로컬 또는 Docker)
./gradlew :app:bootRun
```

#### 방법 2: Docker Compose 사용

```bash
# 로컬 개발용 docker-compose 실행
docker-compose -f docker-compose.local.yml up -d

# 애플리케이션 실행
./gradlew :app:bootRun
```

### Swagger UI 접속

개발 서버 실행 후 브라우저에서 접속:

```
http://localhost:8080/swagger-ui/index.html
```

## 📡 API 엔드포인트

### 인증 (Authentication)

| Method | Endpoint           | 설명               | 인증 필요 |
|--------|--------------------|------------------|-------|
| POST   | `/api/auth/google` | Google OAuth 로그인 | ❌     |
| POST   | `/api/auth/kakao`  | Kakao OAuth 로그인  | ❌     |

### 파티 관리 (Party)

| Method | Endpoint                           | 설명                  | 인증 필요 |
|--------|------------------------------------|---------------------|-------|
| POST   | `/api/parties`                     | 파티 생성 (로그인 사용자)     | ✅     |
| POST   | `/api/parties/guest`               | 파티 생성 (게스트 모드)      | ❌     |
| GET    | `/api/parties/{partyId}`           | 파티 조회 (ID)          | ❌     |
| GET    | `/api/parties/invite/{inviteCode}` | 파티 조회 (초대코드)        | ❌     |
| GET    | `/api/parties/{partyId}/status`    | 파티 매칭 상태            | ❌     |
| GET    | `/api/parties/hosted`              | 내가 만든 파티 목록         | ✅     |
| GET    | `/api/parties/stats`               | 전체 통계 (파티 수, 참가자 수) | ❌     |

### 참가자 관리 (Participant)

| Method | Endpoint                                              | 설명                | 인증 필요 |
|--------|-------------------------------------------------------|-------------------|-------|
| POST   | `/api/parties/{partyId}/join`                         | 파티 참가 (로그인 사용자)   | ✅     |
| POST   | `/api/parties/invite/{inviteCode}/join`               | 파티 참가 (초대코드, 로그인) | ✅     |
| POST   | `/api/parties/{partyId}/guest/join`                   | 파티 참가 (게스트 모드)    | ❌     |
| POST   | `/api/parties/invite/{inviteCode}/guest/join`         | 파티 참가 (초대코드, 게스트) | ❌     |
| GET    | `/api/parties/{partyId}/participants`                 | 참가자 목록 조회         | ❌     |
| DELETE | `/api/parties/{partyId}/participants/{participantId}` | 참가자 삭제            | ❌     |
| GET    | `/api/parties/me`                                     | 내가 참가한 파티 목록      | ✅     |

### 매칭 (Match)

| Method | Endpoint                          | 설명         | 인증 필요 |
|--------|-----------------------------------|------------|-------|
| POST   | `/api/parties/{partyId}/match`    | 마니또 매칭 실행  | ❌     |
| GET    | `/api/parties/{partyId}/my-match` | 내 매칭 결과 조회 | ✅     |

### 게스트 모드

게스트 모드는 **로그인 없이** 파티 생성 및 참가가 가능한 모드입니다.

- 파티 생성: `POST /api/parties/guest`
- 파티 참가: `POST /api/parties/{partyId}/guest/join` 또는
  `POST /api/parties/invite/{inviteCode}/guest/join`

## 🚀 배포 (Deployment)

GitHub Actions를 통해 CI/CD 파이프라인이 구축되어 있습니다. `master` 브랜치에 푸시하면 자동으로 배포됩니다.

- **상세 배포 가이드**: [docs/DEPLOY.md](docs/DEPLOY.md)
- **릴리즈 노트 관리**: [docs/RELEASE.md](docs/RELEASE.md)

## 🗄️ 데이터베이스

### Flyway 마이그레이션

데이터베이스 스키마는 Flyway로 관리됩니다.

**마이그레이션 파일 위치:**

```
app/src/main/resources/db/migration/
├── V1__init_schema.sql      # 초기 스키마
└── V2__add_guest_mode.sql   # 게스트 모드 지원
```

**마이그레이션 실행:**

- 애플리케이션 시작 시 자동 실행
- `spring.flyway.enabled=true` (기본값)

**마이그레이션 상태 확인:**

```bash
# 컨테이너 내에서 실행
docker exec manitto-backend java -jar app.jar --spring.flyway.info
```

### 스키마 구조

- `users`: 사용자 정보
- `party`: 파티 정보
- `participant`: 참가자 정보
- `matched_result`: 매칭 결과

## 🔍 트러블슈팅

자세한 트러블슈팅 가이드는 [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)를 참고하세요.

## 🛠 유용한 명령어

### Docker

```bash
# 로그 확인
docker logs manitto-backend -f                    # 실시간 로그
docker logs manitto-backend --tail 100           # 마지막 100줄
docker logs manitto-backend | grep -i error      # 에러만 필터링

# 컨테이너 관리
docker-compose ps                                 # 상태 확인
docker-compose restart backend                    # 재시작
docker-compose down                               # 중지
docker-compose up -d --build                      # 재빌드 및 시작

# 컨테이너 내부 접속
docker exec -it manitto-backend sh
```

### Gradle

```bash
# 빌드
./gradlew :app:bootJar                            # JAR 파일 생성
./gradlew :app:bootRun                           # 애플리케이션 실행
./gradlew clean build                            # 클린 빌드

# 테스트
./gradlew test                                   # 테스트 실행
```

### 데이터베이스

```bash
# PostgreSQL 접속 (환경변수 사용)
psql -h <DB_HOST> -U <DB_USER> -d manitto

# 테이블 확인
\dt

# 마이그레이션 히스토리 확인
SELECT * FROM flyway_schema_history;
```

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 🤝 기여

버그 리포트나 기능 제안은 Issue로 등록해주세요.

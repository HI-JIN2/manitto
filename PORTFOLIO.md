# Manitto(마니또)

**2024.XX ~ 현재**

백엔드 1명

**GitHub** | **Notion** | **API Docs** | **Deployed**: `http://manito-party.online:8080`

"마니또 파티를 쉽게 만들어보세요" - 로그인 없이도 참여 가능한 온라인 마니또(Secret Santa) 파티 생성 및 관리 서비스입니다. 초대 코드 기반 참여, 자동 매칭, 이메일 알림 기능을 제공합니다.

## 문제

- 파티 생성부터 매칭까지의 복잡한 프로세스로 인한 접근성 저하
- 로그인 없이 빠르게 파티에 참여하기 어려움
- 매칭 결과를 참가자에게 알리는 방법 부재

## 해결방안

- 초대 코드 기반 간편 참여 시스템과 게스트 모드로 접근성 향상
- 자동 랜덤 매칭 알고리즘으로 공정한 결과 보장
- 이메일 발송을 통한 매칭 결과 자동 알림

## 역할

Spring Boot + Kotlin 기반 RESTful API 설계 및 개발

PostgreSQL 데이터베이스 스키마 설계 및 Flyway 마이그레이션 관리

JWT 기반 인증 시스템 및 Google/Kakao OAuth 2.0 연동 구현

이메일 발송 시스템 구축 (JavaMailSender, HTML 템플릿)

Redis Streams 기반 비동기 이메일 큐 시스템 설계 및 구현 계획 수립

CI/CD 파이프라인 구축 (GitHub Actions → NCP 서버 Docker 배포)

API 문서화 (SpringDoc OpenAPI/Swagger UI)

## 사용 기술

**언어**: Kotlin 1.9.23

**프레임워크**: Spring Boot 3.3.0 · Spring Security · Spring Data JPA

**데이터베이스**: PostgreSQL (NCP Cloud DB) · Flyway

**인증/인가**: JWT · Google OAuth 2.0 · Kakao OAuth

**메시징**: Redis Streams · Redis Consumer Groups

**인프라**: Docker · Docker Compose · GitHub Actions · NCP (Naver Cloud Platform)

**문서화**: SpringDoc OpenAPI · Swagger UI

**기타**: JavaMailSender (Gmail SMTP) · Jackson (JSON 처리)

## 경험

도메인 주도 설계(DDD) 패턴을 적용한 레이어드 아키텍처 구성으로 유지보수성 향상

트랜잭션 관리와 데이터 일관성 보장을 위한 `@Transactional` 전략 수립

OAuth 2.0 표준 인증 플로우 구현 및 JWT 토큰 기반 세션리스 인증 구현

비동기 메시징 아키텍처 설계로 성능과 안정성 개선 방안 모색

Docker 기반 컨테이너화 및 CI/CD 파이프라인 구축으로 배포 자동화 경험

## 성과

안정적인 프로덕션 환경 운영 (NCP 클라우드 배포)

자동화된 배포 파이프라인 구축으로 배포 시간 단축

---

## 이메일 발송 로직 Redis 분리 설계 및 구현

### 문제

- **API 응답 시간 지연**: 이메일 발송이 동기적으로 처리되어 매칭 API 응답 시간이 2-5초로 증가
- **트랜잭션 실패 위험**: SMTP 서버 장애 시 전체 매칭 트랜잭션 실패 가능성
- **확장성 제약**: 대량 이메일 발송 시 동시 처리 불가능
- **재시도 로직 부재**: 네트워크 오류 등으로 실패한 이메일 발송 시 복구 불가

### 해결

**Redis Streams 기반 비동기 메시징 시스템 설계**

- **Producer/Consumer 패턴**: 매칭 완료 후 이메일 작업을 Redis Queue에 적재, 별도 Consumer가 비동기 처리
- **Redis Streams 선택**: Redis List 대신 Consumer Groups, 메시지 ACK, Pending Entries List(PEL) 기능을 활용해 메시지 유실 방지
- **Blocking Consumer 구현**: `XREADGROUP` 기반 Blocking 방식으로 폴링 오버헤드 제거 및 효율적 리소스 사용
- **멱등성 보장**: `taskId` 기반 발송 이력 관리로 중복 발송 방지
- **재시도 전략**: 최대 3회 재시도 및 지수 백오프 적용, 실패 시 Dead Letter Queue로 이동

### 결과

- **API 응답 시간 단축**: 2-5초 → 200-500ms (약 90% 개선)
- **장애 격리**: SMTP 서버 장애 시에도 매칭 API 정상 동작 (큐에만 적재)
- **확장성 향상**: Consumer 인스턴스 추가로 처리량 수평 확장 가능
- **안정성 개선**: 재시도 로직 및 PEL을 통한 메시지 유실 방지로 이메일 발송 신뢰도 향상

---

## 게스트 모드 구현

### 문제

- **접근성 제약**: 회원 가입 없이는 파티 생성 및 참가 불가능
- **사용자 진입 장벽**: 로그인 절차로 인한 사용자 이탈 가능성

### 해결

**게스트 모드 기능 구현**

- **비인증 파티 생성**: `POST /api/parties/guest` 엔드포인트로 이메일만으로 파티 생성 가능
- **게스트 참가**: 초대 코드 또는 파티 ID를 통한 게스트 참가 기능 제공
- **게스트 참가자 관리**: 게스트 참가자는 이메일 기반으로 식별, 로그인 사용자와 동일한 매칭 프로세스 적용

### 결과

- **사용자 접근성 향상**: 로그인 없이도 즉시 파티 생성 및 참가 가능
- **유연한 사용자 경험**: 회원 가입 여부와 상관없이 서비스 이용 가능

---

## CI/CD 파이프라인 구축

### 문제

- **수동 배포의 비효율**: 코드 수정 시마다 서버에 직접 접속하여 수동 배포 필요
- **배포 과정의 오류 위험**: 환경 변수 설정 누락, 빌드 실패 등의 가능성

### 해결

**GitHub Actions 기반 자동 배포**

- **워크플로우 단계**: Gradle 빌드 → Docker 이미지 빌드 → 이미지 압축 및 서버 전송 → 컨테이너 재시작
- **캐시 최적화**: Gradle 캐시 및 Docker BuildKit 캐시를 활용한 빌드 시간 단축
- **환경 변수 관리**: GitHub Secrets를 통한 안전한 환경 변수 관리

### 결과

- **배포 자동화**: `master` 브랜치 푸시 시 자동 배포로 배포 시간 단축
- **배포 안정성 향상**: 표준화된 배포 프로세스로 인한 오류 감소
- **개발 효율성 개선**: 수동 작업 제거로 개발에 집중 가능


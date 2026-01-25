# 이메일 발송 로직 Redis 분리 계획서

## 📋 현재 상황 분석

### 현재 구조
- **위치**: `MatchService.matchAndSave()` → `MailService.sendMatchEmail()`
- **방식**: 동기적 처리 (Synchronous)
- **문제점**:
  - 이메일 발송이 느리면 API 응답 시간 증가
  - SMTP 서버 장애 시 전체 트랜잭션 실패 가능성
  - 재시도 로직 없음
  - 대량 이메일 발송 시 성능 저하

## 🎯 목표

1. **비동기 처리**: 이메일 발송을 메인 트랜잭션과 분리
2. **안정성 향상**: 재시도 로직 및 실패 처리
3. **확장성**: 대량 이메일 발송 시에도 안정적인 처리
4. **모니터링**: 이메일 발송 상태 추적 가능

## 🏗️ 설계 계획

### 1단계: Redis 인프라 구축

#### 1.1 의존성 추가
```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-data-redis")
implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") // 선택사항
```

#### 1.2 Redis 설정

**방식 선택**: Redis List (LPUSH/RPOP) vs Redis Streams

- **Redis List**: 단순하고 빠름, 순서 보장
- **Redis Streams**: 더 강력한 기능 (Consumer Groups, 메시지 확인, 재시도)

**✅ 결정: Redis Streams 사용**

**선택 근거:**

Redis Streams를 사용해 Consumer Group 기반 처리, 메시지 ACK 확인, 실패 메시지 재처리를 명확히 관리한다.

단순 List 기반 Queue는 Consumer 장애 시 메시지 유실 가능성이 있으나, Streams는 **Pending Entries List(PEL)**를 통해 처리되지 않은 메시지를 추적할 수 있어 이메일 발송과 같이 **"반드시 한 번은 처리되어야 하는 작업"**에 적합하다고 판단했다.

또한 Consumer Group을 통해 여러 Consumer 인스턴스 간 부하 분산과 장애 복구가 용이하며, `XREADGROUP`을 사용한 Blocking 방식으로 효율적인 메시지 소비가 가능하다.

#### 1.3 Docker Compose에 Redis 추가
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  volumes:
    - redis-data:/data
```

### 2단계: 아키텍처 설계

```
┌─────────────────┐
│  MatchService   │
│  (Producer)     │
└────────┬────────┘
         │
         │ 1. 매칭 완료
         │ 2. 이메일 작업을 Redis Queue에 추가
         ▼
┌─────────────────┐
│   Redis Queue   │
│  (email:queue)  │
└────────┬────────┘
         │
         │ 3. Consumer가 작업 가져오기
         ▼
┌─────────────────┐
│ EmailConsumer   │
│  (Worker)       │
└────────┬────────┘
         │
         │ 4. 실제 이메일 발송
         ▼
┌─────────────────┐
│  MailService    │
│  (SMTP)         │
└─────────────────┘
```

### 3단계: 구현 계획

#### 3.1 DTO 생성
```kotlin
data class EmailTask(
    val taskId: String = UUID.randomUUID().toString(),  // 멱등성 보장을 위한 고유 식별자
    val to: String,
    val target: String,
    val partyName: String?,
    val partyId: Long,  // 파티 ID 추가 (추적 용이)
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
```

**멱등성 보장 설계:**
- `taskId`를 통한 고유 작업 식별
- 이메일 발송 전 `taskId` 기준으로 발송 이력 확인
- 중복 발송 방지를 위한 DB 테이블 또는 Redis Set 활용

#### 3.2 EmailQueueService 생성 (Producer)
- `enqueueEmail()`: 이메일 작업을 Redis에 추가
- Redis Streams 사용 시: `XADD email:queue * ...`
- Redis List 사용 시: `LPUSH email:queue ...`

#### 3.3 EmailConsumer 생성 (Worker)

**✅ 구현 방식: Redis Streams + Consumer Group 기반 Blocking Consumer**

`@Scheduled` 방식의 폴링은 불필요한 CPU 사용과 Redis Streams의 장점을 활용하지 못하므로, **`XREADGROUP`을 사용한 Blocking 방식**으로 구현한다.

**주요 기능:**
- **Blocking 소비**: 메시지가 도착할 때만 Consumer가 깨어나 효율적 처리
- **ACK 처리**: 성공 시 `XACK`로 명확히 처리 완료 표시
- **멱등성 검증**: 이메일 발송 전 `taskId` 기준으로 발송 이력 확인
- **재시도 로직**: 실패 시 최대 3회 재시도 (지수 백오프 적용)
- **Dead Letter Stream**: 최대 재시도 초과 시 별도 Stream으로 이동
- **Pending Entries 복구**: Consumer 장애 시 PEL에 남은 메시지 자동 재처리

**처리 흐름:**
```
1. XREADGROUP으로 메시지 소비 (Blocking)
2. taskId 기준 발송 이력 확인 (중복 체크)
3. 이메일 발송 실행
4. 성공 시: XACK + 발송 이력 저장
5. 실패 시: 재시도 카운트 증가 후 재처리 또는 DLQ 이동
```

#### 3.4 발송 이력 관리 (멱등성 보장)

**DB 테이블 생성:**
```sql
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
```

**또는 Redis Set 활용:**
- `email:sent:{taskId}` 형태로 발송 완료 표시
- TTL 설정으로 오래된 데이터 자동 정리

**Consumer 처리 로직:**
```kotlin
// 이메일 발송 전 중복 체크
if (emailSendLogRepository.existsByTaskId(taskId)) {
    logger.warn("Duplicate email task detected: $taskId")
    return // 이미 발송된 작업이면 스킵
}

// 발송 실행
try {
    mailService.sendMatchEmail(...)
    // 성공 시 이력 저장
    emailSendLogRepository.save(EmailSendLog(taskId, status = SUCCESS))
} catch (e: Exception) {
    // 실패 시 이력 저장 및 재시도
    emailSendLogRepository.save(EmailSendLog(taskId, status = FAILED))
    throw e
}
```

#### 3.5 MailService 리팩토링
- 현재 `sendMatchEmail()` 유지 (Consumer에서 호출)
- 멱등성 검증 로직은 Consumer에서 처리

#### 3.6 MatchService 수정
- `mailService.sendMatchEmail()` → `emailQueueService.enqueueEmail()` 변경
- 트랜잭션 완료 후 즉시 반환
- 이메일 발송은 비동기로 처리되어 API 응답 시간 단축

### 4단계: 고급 기능 (선택사항)

#### 4.1 재시도 전략
- 지수 백오프 (Exponential Backoff)
- 최대 재시도 횟수 제한
- 실패한 작업은 DLQ로 이동

#### 4.2 모니터링 및 장애 대응

**핵심 지표 모니터링:**

1. **Redis Stream 상태**
   - Stream 길이 (대기 중인 작업 수): `XLEN email:queue`
   - Pending Entries 수: `XPENDING email:queue email-consumer-group`
   - Dead Letter Stream 메시지 수: `XLEN email:dlq`

2. **발송 통계**
   - 발송 성공/실패 통계 (시간대별)
   - 평균 처리 시간
   - 재시도율 및 실패율

3. **Consumer 상태**
   - Consumer 인스턴스 수 및 상태
   - Consumer별 처리량
   - Consumer 장애 감지

**장애 대응:**

- **Pending Entries 임계치 초과 시**: 관리자 알림 (Slack Webhook)
- **실패율 임계치 초과 시**: 자동 알림 및 DLQ 메시지 검토
- **Consumer 장애 시**: PEL에 남은 메시지 자동 복구 스크립트 실행
- **정기 모니터링**: 일일/주간 리포트 생성

**구현 예시:**
```kotlin
@Scheduled(fixedRate = 60000) // 1분마다
fun monitorEmailQueue() {
    val pendingCount = redisTemplate.opsForStream()
        .pending("email:queue", "email-consumer-group")
    
    if (pendingCount > 100) {
        slackNotifier.sendAlert("Email queue pending count: $pendingCount")
    }
}
```

#### 4.3 배치 처리
- 여러 이메일을 한 번에 처리하여 성능 향상

## ✅ 장점

### 1. 성능 향상
- ✅ **API 응답 시간 단축**: 이메일 발송 대기 시간 제거
- ✅ **동시 처리**: 여러 이메일을 병렬로 처리 가능
- ✅ **부하 분산**: 이메일 발송 부하를 별도 프로세스로 분리

### 2. 안정성 향상
- ✅ **트랜잭션 분리**: 이메일 발송 실패가 메인 트랜잭션에 영향 없음
- ✅ **재시도 가능**: 실패한 작업 자동 재시도
- ✅ **장애 격리**: SMTP 서버 장애 시에도 API는 정상 동작

### 3. 확장성
- ✅ **수평 확장**: Consumer를 여러 개 실행하여 처리량 증가
- ✅ **대량 처리**: 수백, 수천 개의 이메일도 안정적으로 처리
- ✅ **우선순위 큐**: 긴급 이메일 우선 처리 가능

### 4. 운영 편의성
- ✅ **모니터링**: 큐 상태 확인 가능
- ✅ **디버깅**: 실패한 작업 추적 및 재처리 가능
- ✅ **유연성**: 이메일 발송 로직 변경 시 Consumer만 재시작

## ⚠️ 단점

### 1. 복잡도 증가
- ❌ **인프라 복잡도**: Redis 추가로 인한 운영 복잡도 증가
- ❌ **코드 복잡도**: Producer/Consumer 패턴으로 코드 복잡도 증가
- ❌ **디버깅 난이도**: 비동기 처리로 인한 디버깅 어려움

### 2. 인프라 비용
- ❌ **Redis 서버**: 추가 인프라 비용 발생
- ❌ **리소스 사용**: Consumer 프로세스 추가 리소스 사용

### 3. 지연 시간
- ❌ **최종 일관성**: 이메일 발송이 즉시 실행되지 않음 (몇 초~몇 분 지연 가능)
- ❌ **실시간성 부족**: 발송 상태를 즉시 확인하기 어려움

### 4. 추가 고려사항
- ❌ **메시지 손실**: Redis 장애 시 큐에 있는 작업 손실 가능 (영속성 설정 필요)
  - **대응**: Redis AOF(Append Only File) 활성화, 주기적 백업
- ❌ **모니터링 필요**: 큐 상태, Consumer 상태 모니터링 필요
  - **대응**: 위 4.2 섹션의 모니터링 체계 구축

## 🔄 마이그레이션 전략

### Phase 1: 준비 단계
1. Redis 인프라 구축
2. 의존성 추가 및 설정
3. EmailQueueService, EmailConsumer 기본 구조 구현

### Phase 2: 병행 운영
1. MatchService에서 기존 방식과 새 방식 병행
2. 점진적으로 새 방식으로 전환
3. 모니터링 및 테스트

### Phase 3: 완전 전환
1. 기존 동기 방식 제거
2. Consumer 안정화
3. 모니터링 대시보드 구축

## 📊 예상 효과

### Before (현재)
- API 응답 시간: ~2-5초 (이메일 발송 포함)
- 동시 처리: 제한적
- 장애 영향: SMTP 장애 시 전체 API 실패

### After (Redis 도입 후)
- API 응답 시간: ~200-500ms (큐 추가만)
- 동시 처리: Consumer 수에 따라 확장 가능
- 장애 영향: SMTP 장애 시에도 API 정상 동작 (큐에만 쌓임)

## 🔍 설계 결정 요약

### 기술 스택 선택 근거

**Redis Streams 선택:**
- 현재 트래픽 규모와 운영 복잡도를 고려해 Kafka와 같은 무거운 메시징 시스템 대신 Redis Streams를 선택했다.
- Redis는 이미 캐싱 등으로 사용 중이라면 추가 인프라 비용이 적고, Streams는 이메일 발송과 같은 "최소 한 번 전달 보장"이 필요한 작업에 충분한 기능을 제공한다.

**확장성 고려:**
- 메시지 구조와 Producer/Consumer 분리는 향후 Kafka로 전환하더라도 코드 변경을 최소화할 수 있도록 설계했다.
- EmailTask DTO와 EmailQueueService 인터페이스를 통해 메시징 시스템에 대한 의존성을 추상화했다.

**멱등성 보장:**
- 재시도로 인한 중복 발송을 방지하기 위해 `taskId` 기반 발송 이력 관리와 Consumer 레벨에서의 중복 체크를 설계에 포함했다.

**Consumer 구현:**
- `@Scheduled` 폴링 방식 대신 Redis Streams의 `XREADGROUP` Blocking 방식을 선택하여 효율적인 리소스 사용과 실시간 처리를 보장한다.

## 🎯 결론

**Redis 도입을 권장하는 경우:**
- 이메일 발송이 빈번하고 중요함
- API 응답 시간이 중요함
- 대량 이메일 발송이 예상됨
- 안정성과 확장성이 중요함

**현재 방식을 유지하는 경우:**
- 이메일 발송이 드묾
- 즉시 발송이 중요함
- 인프라 복잡도 증가를 원하지 않음
- 트래픽이 적고 단순한 구조 선호

## 📝 다음 단계

### 구현 우선순위

1. ✅ **Redis 인프라 구축** (Docker Compose, 의존성 추가)
2. ✅ **EmailTask DTO 및 발송 이력 테이블 생성**
3. ✅ **EmailQueueService 구현** (Producer)
4. ✅ **EmailConsumer 구현** (Blocking Consumer, 멱등성 검증 포함)
5. ✅ **재시도 전략 및 DLQ 구현**
6. ✅ **모니터링 및 알림 시스템 구축**
7. ✅ **MatchService 마이그레이션** (병행 운영 → 전환)

### 추가 검토 사항

- Redis 영속성 설정 (AOF/RDB)
- Consumer 스케일링 전략
- 이메일 발송 실패 시 사용자 알림 방식
- 포트폴리오용 1페이지 요약 문서 작성


# 이메일 비동기 발송 솔루션 비교 분석

## 현재 상황 분석

### 현재 구조의 문제점
- **동기적 처리**: `MatchService.matchAndSave()` 메서드에서 이메일 발송이 완료될 때까지 대기
- **API 응답 지연**: 파티당 5-20명 정도의 이메일을 순차 발송하면서 응답 시간 증가
- **트랜잭션 위험**: SMTP 장애 시 전체 매칭 트랜잭션이 실패할 가능성
- **재시도 불가**: 네트워크 오류 시 이메일 발송 실패해도 복구 불가

### 트래픽 예상
- 마니또 파티는 일반적으로 파티당 5-20명
- 동시에 생성되는 파티 수가 많지 않음 (개인/소규모 서비스)
- 이메일 발송 빈도: 매칭 실행 시에만 발생

---

## 솔루션 비교

### 1️⃣ Spring @Async (추천 ⭐)

**가장 간단하고 현재 규모에 적합**

#### 구현 방법
```kotlin
@Service
class MailService {
    @Async
    fun sendMatchEmailAsync(to: String, target: String, partyName: String?) {
        sendMatchEmail(to, target, partyName)
    }
}

@Service
class MatchService {
    @Transactional
    fun matchAndSave(partyId: Long): MatchResultResponse {
        // ... 매칭 로직 ...
        
        // 비동기로 이메일 발송
        results.forEach {
            mailService.sendMatchEmailAsync(it.giver.email, it.receiver.displayName, partyName)
        }
        
        return MatchResultResponse(message = "매칭 완료!")
    }
}
```

#### 설정
```kotlin
@Configuration
@EnableAsync
class AsyncConfig {
    @Bean
    fun taskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("email-async-")
        executor.initialize()
        return executor
    }
}
```

#### 장점 ✅
- **구현 간단**: Spring 기본 기능, 추가 인프라 불필요
- **즉시 적용 가능**: 기존 코드 수정 최소화
- **인프라 비용 없음**: Redis 같은 추가 서비스 불필요
- **충분한 성능**: 현재 트래픽 규모에 적합
- **API 응답 시간 단축**: 이메일 발송 대기 시간 제거

#### 단점 ❌
- **재시도 로직**: 직접 구현 필요 (Spring Retry 등 활용 가능)
- **서버 재시작 시 유실**: 메모리 기반이라 서버 다운 시 작업 손실
- **모니터링**: 별도 구현 필요
- **확장성 제한**: 단일 서버 내에서만 동작

#### 성능
- API 응답 시간: **~200ms 이하** (매칭 로직만 실행)
- 처리량: 파티당 20명 이하의 이메일 동시 발송 가능
- 리소스: 별도 인프라 불필요

---

### 2️⃣ Spring Application Events

**이벤트 기반 느슨한 결합**

#### 구현 방법
```kotlin
// 이벤트 정의
data class MatchCompletedEvent(
    val partyId: Long,
    val results: List<EmailTask>
)

// 이벤트 발행
@Service
class MatchService {
    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher
    
    @Transactional
    fun matchAndSave(partyId: Long): MatchResultResponse {
        // ... 매칭 로직 ...
        
        eventPublisher.publishEvent(MatchCompletedEvent(partyId, emailTasks))
        
        return MatchResultResponse(message = "매칭 완료!")
    }
}

// 이벤트 리스너
@Component
class EmailEventListener(
    private val mailService: MailService
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMatchCompleted(event: MatchCompletedEvent) {
        event.results.forEach {
            mailService.sendMatchEmail(it.email, it.target, it.partyName)
        }
    }
}
```

#### 장점 ✅
- **느슨한 결합**: 이벤트 기반으로 코드 분리
- **트랜잭션 후 처리**: `AFTER_COMMIT`으로 트랜잭션 완료 후 처리 보장
- **확장성**: 여러 리스너로 기능 확장 가능

#### 단점 ❌
- **재시도/영속성**: @Async와 동일한 제약
- **복잡도**: 이벤트 기반 구조 이해 필요

---

### 3️⃣ Database Queue (영속성 보장)

**DB를 큐로 활용, 재시도 가능**

#### 구현 방법
```kotlin
@Entity
class EmailQueue(
    @Id @GeneratedValue
    val id: Long? = null,
    val to: String,
    val target: String,
    val partyName: String?,
    val status: EmailStatus = EmailStatus.PENDING,
    val retryCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Service
class EmailQueueService {
    @Transactional
    fun enqueueEmail(to: String, target: String, partyName: String?) {
        emailQueueRepository.save(EmailQueue(to, target, partyName))
    }
}

@Component
class EmailQueueProcessor(
    private val emailQueueRepository: EmailQueueRepository,
    private val mailService: MailService
) {
    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    fun processEmailQueue() {
        val pendingEmails = emailQueueRepository.findByStatusOrderByCreatedAtAsc(
            EmailStatus.PENDING, 
            PageRequest.of(0, 10)
        )
        
        pendingEmails.forEach { email ->
            try {
                mailService.sendMatchEmail(email.to, email.target, email.partyName)
                email.status = EmailStatus.SUCCESS
            } catch (e: Exception) {
                email.retryCount++
                if (email.retryCount >= 3) {
                    email.status = EmailStatus.FAILED
                }
            }
            emailQueueRepository.save(email)
        }
    }
}
```

#### 장점 ✅
- **영속성 보장**: DB에 저장되어 서버 재시작 시에도 유지
- **재시도 가능**: 실패한 이메일 자동 재처리
- **추적 가능**: 발송 상태와 이력 조회 가능
- **추가 인프라 불필요**: 기존 PostgreSQL 활용

#### 단점 ❌
- **DB 부하**: 폴링 방식으로 DB 쿼리 증가
- **복잡도 증가**: 테이블 생성, 폴링 로직 필요
- **지연 시간**: 최대 5초(설정값) 지연 가능

---

### 4️⃣ Redis Streams (과도할 수 있음)

**강력하지만 복잡도 높음**

#### 장점 ✅
- **고성능**: 메모리 기반으로 매우 빠름
- **확장성**: Consumer Groups로 수평 확장 가능
- **고급 기능**: ACK, PEL, DLQ 등 풍부한 기능

#### 단점 ❌
- **인프라 복잡도**: Redis 서버 운영 필요
- **비용**: Redis 서버 추가 비용
- **구현 복잡도**: Producer/Consumer 패턴 이해 필요
- **과도한 설계**: 현재 트래픽 규모에는 과할 수 있음

---

## 비교표

| 구분 | @Async | Events | DB Queue | Redis |
|------|--------|--------|----------|-------|
| **구현 난이도** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **인프라 비용** | 무료 | 무료 | 무료 | 유료 |
| **영속성** | ❌ | ❌ | ✅ | ⚠️ |
| **재시도** | 수동 | 수동 | 자동 | 자동 |
| **확장성** | 낮음 | 낮음 | 중간 | 높음 |
| **API 응답 시간** | 빠름 | 빠름 | 빠름 | 빠름 |
| **모니터링** | 어려움 | 어려움 | 쉬움 | 쉬움 |
| **현재 규모 적합도** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |

---

## 추천: Spring @Async + Spring Retry

**현재 규모와 요구사항에 가장 적합한 솔루션**

### 구현 계획

1. **@Async 설정 추가**
   - ThreadPoolTaskExecutor 설정
   - 적절한 스레드 풀 크기 설정 (5-10개)

2. **재시도 로직 추가 (선택사항)**
   - Spring Retry 라이브러리 활용
   - 최대 3회 재시도, 지수 백오프

3. **에러 핸들링**
   - AsyncUncaughtExceptionHandler로 실패 이메일 로깅
   - 실패한 경우 DB에 저장하여 추후 수동 처리 (선택)

4. **모니터링**
   - 이메일 발송 성공/실패 로그 기록
   - 필요 시 메트릭 수집 (Prometheus 등)

### 예상 효과
- **API 응답 시간**: 2-5초 → **200ms 이하** (90% 이상 개선)
- **구현 시간**: 1-2시간 (Redis 대비 매우 짧음)
- **운영 복잡도**: 최소 (추가 인프라 없음)

---

## 결론 및 권장사항

### 현재 규모에서는 **Spring @Async** 추천 ✅

**이유:**
1. **충분한 성능**: 파티당 5-20명의 이메일을 비동기로 처리하기에 충분
2. **구현 간단**: Redis 대비 코드 복잡도와 인프라 관리 부담이 적음
3. **비용 효율**: 추가 인프라 비용 없음
4. **빠른 적용**: 즉시 구현 가능

### Redis가 필요한 경우

다음 상황이 발생하면 Redis로 마이그레이션 고려:
- **트래픽 증가**: 동시에 100개 이상의 파티가 매칭되는 경우
- **재시도 중요**: 이메일 발송 실패 시 반드시 재시도해야 하는 경우
- **영속성 필요**: 서버 재시작 시에도 이메일 발송 보장이 필요한 경우
- **수평 확장**: 여러 서버 인스턴스에서 이메일 발송이 필요한 경우

### 단계적 접근

1. **1단계**: Spring @Async로 빠르게 문제 해결 (현재)
2. **2단계**: 트래픽 증가 시 DB Queue로 전환 (영속성 보장)
3. **3단계**: 대규모 트래픽 시 Redis Streams 도입 (확장성)



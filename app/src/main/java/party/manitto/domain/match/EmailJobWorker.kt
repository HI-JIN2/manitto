package party.manitto.domain.match

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import party.manitto.global.entity.EmailJobStatus
import java.time.Duration
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "app.email-worker", name = ["enabled"], havingValue = "true")
class EmailJobWorker(
    private val emailJobRepository: EmailJobRepository,
    private val mailService: MailService
) {
    private val workerId: String = System.getenv("HOSTNAME") ?: "local"

    @Scheduled(fixedDelayString = "\${app.email-worker.poll-interval-ms:1000}")
    fun poll() {
        val jobIds = claimJobs(limit = 20)
        if (jobIds.isEmpty()) return

        jobIds.forEach { processJob(it) }
    }

    @Transactional
    fun claimJobs(limit: Int): List<Long> {
        val now = LocalDateTime.now()
        val jobs = emailJobRepository.findReadyJobsForUpdate(limit)
        jobs.forEach { job ->
            job.status = EmailJobStatus.IN_PROGRESS
            job.lockedAt = now
            job.lockedBy = workerId
            job.updatedAt = now
        }
        return jobs.map { it.id }
    }

    fun processJob(jobId: Long) {
        val payload = loadPayload(jobId) ?: return
        try {
            mailService.sendMatchEmail(
                to = payload.toEmail,
                target = payload.receiverDisplayName,
                partyName = payload.partyName
            )
            markSuccess(jobId)
        } catch (e: Exception) {
            markFailure(jobId, e)
        }
    }

    @Transactional(readOnly = true)
    fun loadPayload(jobId: Long): EmailPayload? {
        val job = emailJobRepository.findWithMatchGraphById(jobId) ?: return null
        if (job.status != EmailJobStatus.IN_PROGRESS) return null

        val matchedResult = job.matchedResult
        return EmailPayload(
            toEmail = job.toEmail,
            receiverDisplayName = matchedResult.receiver.displayName,
            partyName = matchedResult.party.name
        )
    }

    @Transactional
    fun markSuccess(jobId: Long) {
        val now = LocalDateTime.now()
        val job = emailJobRepository.findById(jobId).orElse(null) ?: return
        job.status = EmailJobStatus.SUCCESS
        job.sentAt = now
        job.lockedAt = null
        job.lockedBy = null
        job.lastError = null
        job.updatedAt = now
    }

    @Transactional
    fun markFailure(jobId: Long, e: Exception) {
        val now = LocalDateTime.now()
        val job = emailJobRepository.findById(jobId).orElse(null) ?: return

        val nextAttempt = job.attemptCount + 1
        val maxAttempts = (System.getenv("EMAIL_JOB_MAX_ATTEMPTS") ?: "5").toIntOrNull() ?: 5

        job.attemptCount = nextAttempt
        job.lastError = (e.message ?: e.javaClass.simpleName).take(2000)

        if (nextAttempt >= maxAttempts) {
            job.status = EmailJobStatus.FAILED
            job.nextRunAt = now
        } else {
            job.status = EmailJobStatus.RETRY
            job.nextRunAt = now.plus(calculateBackoff(nextAttempt))
        }

        job.lockedAt = null
        job.lockedBy = null
        job.updatedAt = now
    }

    private fun calculateBackoff(attempt: Int): Duration {
        val seconds = when (attempt) {
            1 -> 5
            2 -> 30
            3 -> 120
            4 -> 600
            else -> 1800
        }
        return Duration.ofSeconds(seconds.toLong())
    }

    data class EmailPayload(
        val toEmail: String,
        val receiverDisplayName: String,
        val partyName: String
    )
}

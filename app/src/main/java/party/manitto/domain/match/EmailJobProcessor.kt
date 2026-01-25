package party.manitto.domain.match

import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import party.manitto.config.EmailWorkerProperties
import party.manitto.global.entity.EmailJobStatus
import java.time.Duration
import java.time.LocalDateTime

@Service
class EmailJobProcessor(
    private val emailJobRepository: EmailJobRepository,
    private val mailService: MailService,
    private val transactionTemplate: TransactionTemplate,
    private val workerProperties: EmailWorkerProperties
) {
    private val workerId: String = System.getenv("HOSTNAME") ?: "local"

    fun claimJobs(): List<Long> {
        val now = LocalDateTime.now()
        val batchSize = workerProperties.batchSize
        val lockTimeoutMinutes = workerProperties.lockTimeoutMinutes

        return transactionTemplate.execute {
            val jobs = emailJobRepository.findReadyJobsForUpdate(batchSize, lockTimeoutMinutes)
            jobs.forEach { job ->
                job.status = EmailJobStatus.IN_PROGRESS
                job.lockedAt = now
                job.lockedBy = workerId
                job.updatedAt = now
            }
            jobs.map { it.id }
        } ?: emptyList()
    }

    /**
     * Processes a single email job.
     *
     * Delivery semantics: at-least-once.
     * If the worker crashes after sending the email but before marking SUCCESS,
     * the job can be re-processed after the lock timeout, potentially sending a duplicate.
     */
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

    private fun loadPayload(jobId: Long): EmailPayload? {
        return transactionTemplate.execute {
            val job = emailJobRepository.findWithMatchGraphById(jobId) ?: return@execute null
            if (job.status != EmailJobStatus.IN_PROGRESS) return@execute null

            val matchedResult = job.matchedResult
            EmailPayload(
                toEmail = job.toEmail,
                receiverDisplayName = matchedResult.receiver.displayName,
                partyName = matchedResult.party.name
            )
        }
    }

    private fun markSuccess(jobId: Long) {
        val now = LocalDateTime.now()
        transactionTemplate.executeWithoutResult {
            val job = emailJobRepository.findById(jobId).orElse(null) ?: return@executeWithoutResult
            job.status = EmailJobStatus.SUCCESS
            job.sentAt = now
            job.lockedAt = null
            job.lockedBy = null
            job.lastError = null
            job.updatedAt = now
        }
    }

    private fun markFailure(jobId: Long, e: Exception) {
        val now = LocalDateTime.now()
        val maxAttempts = workerProperties.maxAttempts

        transactionTemplate.executeWithoutResult {
            val job = emailJobRepository.findById(jobId).orElse(null) ?: return@executeWithoutResult
            val nextAttempt = job.attemptCount + 1

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

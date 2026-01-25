package party.manitto.domain.match

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executor

@Component
@ConditionalOnProperty(prefix = "app.email-worker", name = ["enabled"], havingValue = "true")
class EmailJobWorker(
    private val emailJobProcessor: EmailJobProcessor,
    @Qualifier("taskExecutor")
    private val taskExecutor: Executor
) {

    @Scheduled(fixedDelayString = "\${app.email-worker.poll-interval-ms:1000}")
    fun poll() {
        val jobIds = emailJobProcessor.claimJobs()
        if (jobIds.isEmpty()) return

        jobIds.forEach { jobId ->
            taskExecutor.execute { emailJobProcessor.processJob(jobId) }
        }
    }
}

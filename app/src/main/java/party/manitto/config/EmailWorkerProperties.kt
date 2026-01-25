package party.manitto.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.email-worker")
data class EmailWorkerProperties(
    val enabled: Boolean = false,
    val pollIntervalMs: Long = 1000,
    val batchSize: Int = 20,
    val maxAttempts: Int = 5,
    val lockTimeoutMinutes: Int = 10
)

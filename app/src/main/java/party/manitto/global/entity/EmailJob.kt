package party.manitto.global.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

@Entity
class EmailJob(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_result_id", nullable = false, unique = true)
    val matchedResult: MatchedResult,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    val party: Party,

    @Column(name = "to_email", nullable = false)
    val toEmail: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EmailJobStatus = EmailJobStatus.PENDING,

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,

    @Column(name = "next_run_at", nullable = false)
    var nextRunAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "locked_at")
    var lockedAt: LocalDateTime? = null,

    @Column(name = "locked_by", length = 100)
    var lockedBy: String? = null,

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

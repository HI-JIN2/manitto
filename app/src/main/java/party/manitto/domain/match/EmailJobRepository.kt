package party.manitto.domain.match

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import party.manitto.global.entity.EmailJob

interface EmailJobRepository : JpaRepository<EmailJob, Long> {

    @Query(
        value = """
            SELECT *
            FROM email_job
            WHERE status IN ('PENDING', 'RETRY')
              AND next_run_at <= NOW()
              AND (locked_at IS NULL OR locked_at < NOW() - INTERVAL '10 minutes')
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun findReadyJobsForUpdate(@Param("limit") limit: Int): List<EmailJob>

    @Query(
        """
            SELECT j
            FROM EmailJob j
            JOIN FETCH j.matchedResult mr
            JOIN FETCH mr.giver
            JOIN FETCH mr.receiver
            JOIN FETCH mr.party
            WHERE j.id = :id
        """
    )
    fun findWithMatchGraphById(@Param("id") id: Long): EmailJob?
}

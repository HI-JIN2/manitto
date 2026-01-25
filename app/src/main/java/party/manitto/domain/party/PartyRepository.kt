package party.manitto.domain.party

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

interface PartyRepository : JpaRepository<Party, Long> {
    fun findByInviteCode(inviteCode: String): Party?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByInviteCodeForUpdate(inviteCode: String): Party?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Party p WHERE p.id = :partyId")
    fun findByIdForUpdate(@Param("partyId") partyId: Long): Party?

    fun existsByInviteCode(inviteCode: String): Boolean
    fun findByHost(host: User): List<Party>
}

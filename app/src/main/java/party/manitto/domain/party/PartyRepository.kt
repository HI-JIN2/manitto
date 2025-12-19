package party.manitto.domain.party

import org.springframework.data.jpa.repository.JpaRepository
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

interface PartyRepository : JpaRepository<Party, Long> {
    fun findByInviteCode(inviteCode: String): Party?
    fun existsByInviteCode(inviteCode: String): Boolean
    fun findByHost(host: User): List<Party>
}
package party.manitto.domain.party

import org.springframework.data.jpa.repository.JpaRepository
import party.manitto.global.entity.Party

interface PartyRepository : JpaRepository<Party, Long>
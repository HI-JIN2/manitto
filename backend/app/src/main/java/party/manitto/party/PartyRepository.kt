package party.manitto.party

import org.springframework.data.jpa.repository.JpaRepository

interface PartyRepository : JpaRepository<Party, Long>
package party.manitto.domain.participant

import party.manitto.global.entity.Participant
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipantRepository : JpaRepository<Participant, Long>
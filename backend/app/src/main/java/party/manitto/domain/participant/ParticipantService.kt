package party.manitto.domain.participant

import org.springframework.stereotype.Service
import party.manitto.global.entity.Participant
import party.manitto.domain.party.PartyRepository

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val partyRepository: PartyRepository
) {
    fun joinParty(partyId: Long, email: String): ParticipantResponse {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("파티가 존재하지 않습니다.") }

        val participant = participantRepository.save(Participant(email = email, party = party))
        return ParticipantResponse(id = participant.id, email = participant.email)
    }

    fun getParticipants(partyId: Long): List<ParticipantResponse> {
        return participantRepository.findByPartyId(partyId)
            .map { ParticipantResponse(id = it.id, email = it.email) }
    }
}
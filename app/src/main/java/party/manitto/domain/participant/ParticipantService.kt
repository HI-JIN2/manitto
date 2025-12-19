package party.manitto.domain.participant

import org.springframework.stereotype.Service
import party.manitto.global.entity.Participant
import party.manitto.global.entity.User
import party.manitto.domain.party.PartyRepository

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val partyRepository: PartyRepository
) {
    fun joinParty(partyId: Long, user: User, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("파티가 존재하지 않습니다.") }

        val existing = participantRepository.findByPartyIdAndUser(partyId, user)
        if (existing != null) {
            throw IllegalArgumentException("이미 이 파티에 참가한 사용자입니다.")
        }

        val participant = participantRepository.save(
            Participant(user = user, party = party, nickname = nickname)
        )
        return ParticipantResponse.from(participant)
    }

    fun getParticipants(partyId: Long): List<ParticipantResponse> {
        return participantRepository.findByPartyId(partyId)
            .map { ParticipantResponse.from(it) }
    }

    fun getMyParties(user: User): List<ParticipantResponse> {
        return participantRepository.findByUser(user)
            .map { ParticipantResponse.from(it) }
    }
}
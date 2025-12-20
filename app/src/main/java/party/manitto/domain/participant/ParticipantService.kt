package party.manitto.domain.participant

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.participant.dto.ParticipantResponse
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party
import party.manitto.global.entity.User
import party.manitto.domain.party.PartyRepository

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val partyRepository: PartyRepository,
    private val matchedResultRepository: MatchedResultRepository
) {
    @Transactional
    fun joinPartyById(partyId: Long, user: User, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("파티가 존재하지 않습니다.") }
        return joinParty(party, user, nickname)
    }

    @Transactional
    fun joinPartyByInviteCode(inviteCode: String, user: User, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findByInviteCode(inviteCode)
            ?: throw IllegalArgumentException("유효하지 않은 초대 코드입니다.")
        return joinParty(party, user, nickname)
    }

    private fun joinParty(party: Party, user: User, nickname: String?): ParticipantResponse {
        // 이미 매칭된 파티에는 참가 불가
        if (matchedResultRepository.existsByParty(party)) {
            throw IllegalArgumentException("이미 매칭이 완료된 파티에는 참가할 수 없습니다.")
        }

        val existing = participantRepository.findByPartyIdAndUser(party.id, user)
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

    @Transactional
    fun joinPartyAsGuest(partyId: Long, name: String, email: String, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("파티가 존재하지 않습니다.") }
        return joinPartyAsGuest(party, name, email, nickname)
    }

    @Transactional
    fun joinPartyAsGuestByInviteCode(inviteCode: String, name: String, email: String, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findByInviteCode(inviteCode)
            ?: throw IllegalArgumentException("유효하지 않은 초대 코드입니다.")
        return joinPartyAsGuest(party, name, email, nickname)
    }

    private fun joinPartyAsGuest(party: Party, name: String, email: String, nickname: String?): ParticipantResponse {
        // 이미 매칭된 파티에는 참가 불가
        if (matchedResultRepository.existsByParty(party)) {
            throw IllegalArgumentException("이미 매칭이 완료된 파티에는 참가할 수 없습니다.")
        }

        // 같은 이메일로 이미 참가했는지 확인
        val existing = participantRepository.findByPartyIdAndGuestEmail(party.id, email)
        if (existing != null) {
            throw IllegalArgumentException("이미 이 파티에 참가한 이메일입니다.")
        }

        val participant = participantRepository.save(
            Participant(
                user = null,
                party = party,
                nickname = nickname,
                guestName = name,
                guestEmail = email
            )
        )
        return ParticipantResponse.from(participant)
    }
}

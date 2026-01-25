package party.manitto.domain.participant

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.participant.dto.ParticipantResponse
import party.manitto.domain.party.PartyRepository
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val partyRepository: PartyRepository,
    private val matchedResultRepository: MatchedResultRepository
) {
    private fun validatePartyIsJoinable(party: Party) {
        if (matchedResultRepository.existsByParty(party)) {
            throw CustomException(ErrorCode.ALREADY_MATCHED)
        }

        val currentCount = participantRepository.countByPartyId(party.id)
        if (currentCount >= Party.MAX_PARTICIPANTS) {
            throw CustomException(ErrorCode.PARTY_PARTICIPANT_LIMIT_EXCEEDED)
        }
    }

    @Transactional
    fun joinPartyById(partyId: Long, user: User, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findByIdForUpdate(partyId)
            ?: throw CustomException(ErrorCode.PARTY_NOT_FOUND)
        return joinParty(party, user, nickname)
    }

    @Transactional
    fun joinPartyByInviteCode(inviteCode: String, user: User, nickname: String? = null): ParticipantResponse {
        val party = partyRepository.findByInviteCodeForUpdate(inviteCode)
            ?: throw CustomException(ErrorCode.INVALID_INVITE_CODE)
        return joinParty(party, user, nickname)
    }

    private fun joinParty(party: Party, user: User, nickname: String?): ParticipantResponse {
        validatePartyIsJoinable(party)

        val existing = participantRepository.findByPartyIdAndUser(party.id, user)
        if (existing != null) {
            throw CustomException(ErrorCode.ALREADY_JOINED_PARTY)
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
    fun joinPartyAsGuest(partyId: Long, name: String, email: String): ParticipantResponse {
        val party = partyRepository.findByIdForUpdate(partyId)
            ?: throw CustomException(ErrorCode.PARTY_NOT_FOUND)
        return joinPartyAsGuest(party, name, email)
    }

    @Transactional
    fun joinPartyAsGuestByInviteCode(
        inviteCode: String,
        name: String,
        email: String
    ): ParticipantResponse {
        val party = partyRepository.findByInviteCodeForUpdate(inviteCode)
            ?: throw CustomException(ErrorCode.INVALID_INVITE_CODE)
        return joinPartyAsGuest(party, name, email)
    }

    private fun joinPartyAsGuest(party: Party, name: String, email: String): ParticipantResponse {
        validatePartyIsJoinable(party)

        // 같은 이메일로 이미 참가했는지 확인
        val existing = participantRepository.findByPartyIdAndGuestEmail(party.id, email)
        if (existing != null) {
            throw CustomException(ErrorCode.ALREADY_JOINED_EMAIL)
        }

        val participant = participantRepository.save(
            Participant(
                user = null,
                party = party,
                guestName = name,
                guestEmail = email
            )
        )
        return ParticipantResponse.from(participant)
    }

    @Transactional
    fun deleteParticipant(partyId: Long, participantId: Long) {
        val participant = participantRepository.findById(participantId)
            .orElseThrow { CustomException(ErrorCode.PARTICIPANT_NOT_FOUND) }

        if (participant.party.id != partyId) {
            throw CustomException(ErrorCode.PARTICIPANT_PARTY_MISMATCH)
        }

        // 매칭 완료된 파티에서는 삭제 불가
        if (matchedResultRepository.existsByParty(participant.party)) {
            throw CustomException(ErrorCode.ALREADY_MATCHED)
        }

        participantRepository.delete(participant)
    }
}

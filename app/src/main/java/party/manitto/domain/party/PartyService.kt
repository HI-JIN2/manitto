package party.manitto.domain.party

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.domain.party.dto.GuestParticipantRequest
import party.manitto.domain.party.dto.PartyResponse
import party.manitto.domain.party.dto.PartyStatsResponse
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party
import party.manitto.global.entity.User
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val matchedResultRepository: MatchedResultRepository,
    private val participantRepository: ParticipantRepository
) {

    @Transactional
    fun createParty(name: String, host: User): PartyResponse {
        val inviteCode = generateUniqueInviteCode()
        val newParty = Party(name = name, inviteCode = inviteCode, host = host)
        val saved = partyRepository.save(newParty)
        return PartyResponse.from(saved)
    }

    @Transactional
    fun createGuestParty(
        name: String,
        hostName: String,
        hostEmail: String,
        participants: List<GuestParticipantRequest>? = null
    ): PartyResponse {
        val hostEmailLower = hostEmail.lowercase()
        val uniqueAdditionalCount = participants
            .orEmpty()
            .map { it.email.lowercase() }
            .filter { it != hostEmailLower }
            .distinct()
            .size
        if (1 + uniqueAdditionalCount > Party.MAX_PARTICIPANTS) {
            throw CustomException(ErrorCode.PARTY_PARTICIPANT_LIMIT_EXCEEDED)
        }

        val inviteCode = generateUniqueInviteCode()
        val newParty = Party(name = name, inviteCode = inviteCode, host = null)
        val saved = partyRepository.save(newParty)

        // 게스트 호스트를 첫 번째 참가자로 추가
        val hostParticipant = Participant(
            user = null,
            party = saved,
            nickname = null,
            guestName = hostName,
            guestEmail = hostEmail
        )
        participantRepository.save(hostParticipant)
        saved.participants.add(hostParticipant)

        // 추가 게스트 참가자들 자동 참여 (이메일 중복 방지)
        val seenEmails = mutableSetOf(hostEmailLower)
        participants
            .orEmpty()
            .forEach { guest ->
                val emailLower = guest.email.lowercase()
                if (emailLower in seenEmails) {
                    return@forEach
                }
                seenEmails.add(emailLower)

                val participant = Participant(
                    user = null,
                    party = saved,
                    guestName = guest.name,
                    guestEmail = guest.email
                )
                participantRepository.save(participant)
                saved.participants.add(participant)
            }

        // 저장된 파티를 다시 조회하여 participants 컬렉션을 로드
        val partyWithParticipants = partyRepository.findById(saved.id)
            .orElseThrow { CustomException(ErrorCode.PARTY_NOT_FOUND) }

        return PartyResponse.from(partyWithParticipants)
    }

    private fun generateUniqueInviteCode(): String {
        var code: String
        do {
            code = Party.generateInviteCode()
        } while (partyRepository.existsByInviteCode(code))
        return code
    }

    fun getPartyById(partyId: Long): PartyResponse {
        val party = partyRepository.findById(partyId)
            .orElseThrow { CustomException(ErrorCode.PARTY_NOT_FOUND) }
        return PartyResponse.from(party)
    }

    fun getPartyByInviteCode(inviteCode: String): PartyResponse {
        val party = partyRepository.findByInviteCode(inviteCode)
            ?: throw CustomException(ErrorCode.INVALID_INVITE_CODE)
        return PartyResponse.from(party)
    }

    fun getMyHostedParties(host: User): List<PartyResponse> {
        return partyRepository.findByHost(host).map { PartyResponse.from(it) }
    }

    fun isMatched(partyId: Long): Boolean {
        val party = partyRepository.findById(partyId)
            .orElseThrow { CustomException(ErrorCode.PARTY_NOT_FOUND) }
        return matchedResultRepository.existsByParty(party)
    }

    fun isHost(partyId: Long, user: User): Boolean {
        val party = partyRepository.findById(partyId)
            .orElseThrow { CustomException(ErrorCode.PARTY_NOT_FOUND) }
        return party.host?.id == user.id
    }

    fun getStats(): PartyStatsResponse {
        val partyCount = partyRepository.count()
        val participantCount = participantRepository.count()
        return PartyStatsResponse(
            partyCount = partyCount,
            participantCount = participantCount
        )
    }
}

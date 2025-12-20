package party.manitto.domain.party

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.domain.party.dto.PartyResponse
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

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
    fun createGuestParty(name: String, hostName: String, hostEmail: String): PartyResponse {
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

        // participants 컬렉션에 직접 추가 (JPA 관계 동기화)
        saved.participants.add(hostParticipant)

        // 저장된 파티를 다시 조회하여 participants 컬렉션을 로드
        val partyWithParticipants = partyRepository.findById(saved.id)
            .orElseThrow { IllegalStateException("Party not found after creation") }

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
            .orElseThrow { IllegalArgumentException("해당 파티가 존재하지 않습니다.") }
        return PartyResponse.from(party)
    }

    fun getPartyByInviteCode(inviteCode: String): PartyResponse {
        val party = partyRepository.findByInviteCode(inviteCode)
            ?: throw IllegalArgumentException("유효하지 않은 초대 코드입니다.")
        return PartyResponse.from(party)
    }

    fun getMyHostedParties(host: User): List<PartyResponse> {
        return partyRepository.findByHost(host).map { PartyResponse.from(it) }
    }

    fun isMatched(partyId: Long): Boolean {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("해당 파티가 존재하지 않습니다.") }
        return matchedResultRepository.existsByParty(party)
    }

    fun isHost(partyId: Long, user: User): Boolean {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("해당 파티가 존재하지 않습니다.") }
        return party.host?.id == user.id
    }
}
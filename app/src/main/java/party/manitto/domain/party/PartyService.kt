package party.manitto.domain.party

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val matchedResultRepository: MatchedResultRepository
) {

    @Transactional
    fun createParty(name: String, host: User): PartyResponse {
        val inviteCode = generateUniqueInviteCode()
        val newParty = Party(name = name, inviteCode = inviteCode, host = host)
        val saved = partyRepository.save(newParty)
        return PartyResponse.from(saved)
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
        val party = partyRepository.findByInviteCode(inviteCode.uppercase())
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
        return party.host.id == user.id
    }
}
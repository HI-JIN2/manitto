package party.manitto.domain.party

import org.springframework.stereotype.Service
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.global.entity.Party

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val matchedResultRepository: MatchedResultRepository
) {

    fun createParty(name: String, password: String): Party {
        val newParty = Party(name = name, password = password)
        return partyRepository.save(newParty)
    }

    fun getAllParties(): List<Party> = partyRepository.findAll()

    fun isMatched(partyId: Long): Boolean {
        val party = partyRepository.findById(partyId)
            .orElseThrow { IllegalArgumentException("해당 파티가 존재하지 않습니다.") }

        // 매칭 결과가 하나라도 존재하면 매칭 완료로 판단
        return matchedResultRepository.existsByParty(party)
    }
}
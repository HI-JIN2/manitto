package party.manitto.domain.party

import org.springframework.stereotype.Service
import party.manitto.global.entity.Party

@Service
class PartyService(
    private val partyRepository: PartyRepository
) {

    fun createParty(name: String, password: String): Party {
        val newParty = Party(name = name, password = password)
        return partyRepository.save(newParty)
    }

    fun getAllParties(): List<Party> = partyRepository.findAll()
}
package party.manitto.domain.party

import org.springframework.web.bind.annotation.*
import party.manitto.global.entity.Party

@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = ["http://localhost:*"])
class PartyController(
    private val partyService: PartyService
) {
    data class CreatePartyRequest(val name: String, val password: String)

    @PostMapping
    fun createParty(@RequestBody req: CreatePartyRequest): Party {
        return partyService.createParty(req.name, req.password)
    }

    @GetMapping
    fun getAllParties(): List<Party> = partyService.getAllParties()
}
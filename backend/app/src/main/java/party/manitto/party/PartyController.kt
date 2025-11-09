package party.manitto.party

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = ["http://localhost:5173"])
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
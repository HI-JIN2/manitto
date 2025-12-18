package party.manitto.domain.party

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import party.manitto.global.entity.Party

@RestController
@RequestMapping("/api/parties")
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

    @GetMapping("/{partyId}/status")
    fun getPartyStatus(@PathVariable partyId: Long): ResponseEntity<Map<String, Boolean>> {
        val isMatched = partyService.isMatched(partyId)
        return ResponseEntity.ok(mapOf("matched" to isMatched))
    }
}
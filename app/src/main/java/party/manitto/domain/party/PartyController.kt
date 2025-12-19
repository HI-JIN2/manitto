package party.manitto.domain.party

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/parties")
class PartyController(
    private val partyService: PartyService
) {
    data class CreatePartyRequest(val name: String)

    @PostMapping
    fun createParty(
        @RequestBody req: CreatePartyRequest,
        @AuthenticationPrincipal user: User
    ): PartyResponse {
        return partyService.createParty(req.name, user)
    }

    @GetMapping("/{partyId}")
    fun getParty(@PathVariable partyId: Long): PartyResponse {
        return partyService.getPartyById(partyId)
    }

    @GetMapping("/invite/{inviteCode}")
    fun getPartyByInviteCode(@PathVariable inviteCode: String): PartyResponse {
        return partyService.getPartyByInviteCode(inviteCode)
    }

    @GetMapping("/hosted")
    fun getMyHostedParties(@AuthenticationPrincipal user: User): List<PartyResponse> {
        return partyService.getMyHostedParties(user)
    }

    @GetMapping("/{partyId}/status")
    fun getPartyStatus(@PathVariable partyId: Long): ResponseEntity<Map<String, Boolean>> {
        val isMatched = partyService.isMatched(partyId)
        return ResponseEntity.ok(mapOf("matched" to isMatched))
    }
}
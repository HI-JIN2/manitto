package party.manitto.domain.participant

import party.manitto.global.entity.Participant
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = ["http://localhost:*"])
class ParticipantController(
    private val participantService: ParticipantService
) {
    data class JoinRequest(val email: String)

    @PostMapping("/{partyId}/join")
    fun joinParty(
        @PathVariable partyId: Long,
        @RequestBody req: JoinRequest
    ): Participant {
        return participantService.joinParty(partyId, req.email)
    }
}
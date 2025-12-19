package party.manitto.domain.participant

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/parties")
class ParticipantController(
    private val participantService: ParticipantService
) {
    data class JoinRequest(val nickname: String? = null)

    @PostMapping("/{partyId}/join")
    fun joinParty(
        @PathVariable partyId: Long,
        @RequestBody(required = false) req: JoinRequest?,
        @AuthenticationPrincipal user: User
    ): ParticipantResponse {
        return participantService.joinParty(partyId, user, req?.nickname)
    }

    @GetMapping("/{partyId}/participants")
    fun getParticipants(@PathVariable partyId: Long): List<ParticipantResponse> {
        return participantService.getParticipants(partyId)
    }

    @GetMapping("/me")
    fun getMyParties(@AuthenticationPrincipal user: User): List<ParticipantResponse> {
        return participantService.getMyParties(user)
    }
}
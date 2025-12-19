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

    // 파티 ID로 참가 (내부 용도)
    @PostMapping("/{partyId}/join")
    fun joinPartyById(
        @PathVariable partyId: Long,
        @RequestBody(required = false) req: JoinRequest?,
        @AuthenticationPrincipal user: User
    ): ParticipantResponse {
        return participantService.joinPartyById(partyId, user, req?.nickname)
    }

    // 초대 코드로 참가 (링크 또는 수동 입력)
    @PostMapping("/invite/{inviteCode}/join")
    fun joinPartyByInviteCode(
        @PathVariable inviteCode: String,
        @RequestBody(required = false) req: JoinRequest?,
        @AuthenticationPrincipal user: User
    ): ParticipantResponse {
        return participantService.joinPartyByInviteCode(inviteCode, user, req?.nickname)
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
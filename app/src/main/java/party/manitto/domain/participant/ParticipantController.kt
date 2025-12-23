package party.manitto.domain.participant

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import party.manitto.domain.participant.dto.GuestJoinPartyRequest
import party.manitto.domain.participant.dto.JoinPartyRequest
import party.manitto.domain.participant.dto.ParticipantResponse
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/parties")
class ParticipantController(
    private val participantService: ParticipantService
) {
    // 파티 ID로 참가 (내부 용도)
    @PostMapping("/{partyId}/join")
    fun joinPartyById(
        @PathVariable partyId: Long,
        @RequestBody(required = false) req: JoinPartyRequest?,
        @AuthenticationPrincipal user: User
    ): ParticipantResponse {
        return participantService.joinPartyById(partyId, user, req?.nickname)
    }

    // 초대 코드로 참가 (링크 또는 수동 입력)
    @PostMapping("/invite/{inviteCode}/join")
    fun joinPartyByInviteCode(
        @PathVariable inviteCode: String,
        @RequestBody(required = false) req: JoinPartyRequest?,
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

    // 게스트 모드: 로그인 없이 파티 참가
    @PostMapping("/{partyId}/guest/join")
    fun joinPartyAsGuest(
        @PathVariable partyId: Long,
        @RequestBody req: GuestJoinPartyRequest
    ): ParticipantResponse {
        return participantService.joinPartyAsGuest(partyId, req.name, req.email)
    }

    @DeleteMapping("/{partyId}/participants/{participantId}")
    fun deleteParticipant(
        @PathVariable partyId: Long,
        @PathVariable participantId: Long
    ) {
        participantService.deleteParticipant(partyId, participantId)
    }

    @PostMapping("/invite/{inviteCode}/guest/join")
    fun joinPartyAsGuestByInviteCode(
        @PathVariable inviteCode: String,
        @RequestBody req: GuestJoinPartyRequest
    ): ParticipantResponse {
        return participantService.joinPartyAsGuestByInviteCode(inviteCode, req.name, req.email)
    }
}
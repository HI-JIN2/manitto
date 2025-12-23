package party.manitto.domain.party

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import party.manitto.domain.party.dto.*
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/parties")
class PartyController(
    private val partyService: PartyService
) {
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
    fun getPartyStatus(@PathVariable partyId: Long): PartyStatusResponse {
        val isMatched = partyService.isMatched(partyId)
        return PartyStatusResponse(matched = isMatched)
    }

    // 게스트 모드: 로그인 없이 파티 생성
    @PostMapping("/guest")
    fun createGuestParty(@RequestBody req: GuestCreatePartyRequest): PartyResponse {
        return partyService.createGuestParty(req.name, req.hostName, req.hostEmail)
    }
}
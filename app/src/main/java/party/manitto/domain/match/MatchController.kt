package party.manitto.domain.match

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import party.manitto.domain.match.dto.MatchResultResponse
import party.manitto.domain.match.dto.MyMatchResponse
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/parties")
class MatchController(
    private val matchService: MatchService
) {
    @PostMapping("/{partyId}/match")
    fun matchAndNotify(@PathVariable partyId: Long): MatchResultResponse {
        return matchService.matchAndSave(partyId)
    }
    
    @GetMapping("/{partyId}/my-match")
    fun getMyMatch(
        @PathVariable partyId: Long,
        @AuthenticationPrincipal user: User
    ): MyMatchResponse {
        return matchService.getMyMatch(partyId, user)
    }
}
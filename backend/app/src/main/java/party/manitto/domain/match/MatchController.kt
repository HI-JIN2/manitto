package party.manitto.domain.match

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parties")
class MatchController(
    private val matchService: MatchService,
    private val mailService: MailService
) {
    @PostMapping("/{partyId}/match")
    fun matchAndNotify(@PathVariable partyId: Long): String {
        return matchService.matchAndSave(partyId)
    }
}
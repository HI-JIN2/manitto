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
        val pairs = matchService.matchParticipants(partyId)

        pairs.forEach { (giver, receiver) ->
            mailService.sendMatchEmail(giver.email, receiver.email)
        }

        return "매칭 완료! 이메일 발송됨."
    }
}
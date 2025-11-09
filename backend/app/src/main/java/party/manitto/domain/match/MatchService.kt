package party.manitto.domain.match

import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.entity.Participant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.global.entity.MatchedResult
import kotlin.random.Random

@Service
class MatchService(
    private val participantRepository: ParticipantRepository,
    private val matchedResultRepository: MatchedResultRepository,
    private val mailService: MailService
) {
    @Transactional
    fun matchAndSave(partyId: Long): String {
        val participants = participantRepository.findByPartyId(partyId)
        require(participants.size > 1) { "참여자가 2명 이상이어야 합니다." }

        val shuffled = participants.toMutableList()
        do {
            shuffled.shuffle(Random(System.nanoTime()))
        } while (shuffled.zip(participants).any { it.first.id == it.second.id })

        val results = participants.zip(shuffled).map { (giver, receiver) ->
            MatchedResult(giver = giver, receiver = receiver, party = giver.party)
        }

        matchedResultRepository.saveAll(results)

        results.forEach {
            mailService.sendMatchEmail(it.giver.email, it.receiver.email)
        }

        return "매칭 완료 및 이메일 발송 성공!"
    }
}
package party.manitto.domain.match

import party.manitto.domain.match.dto.MatchResultResponse
import party.manitto.domain.match.dto.MyMatchResponse
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.entity.MatchedResult
import party.manitto.global.entity.Participant
import party.manitto.global.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class MatchService(
    private val participantRepository: ParticipantRepository,
    private val matchedResultRepository: MatchedResultRepository,
    private val mailService: MailService
) {
    @Transactional
    fun matchAndSave(partyId: Long): MatchResultResponse {
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

        // 파티 이름 가져오기
        val party = participants.firstOrNull()?.party
        val partyName = party?.name

        // 매칭 결과 이메일 발송
        results.forEach {
            mailService.sendMatchEmail(it.giver.email, it.receiver.displayName, partyName)
        }

        return MatchResultResponse(message = "매칭 완료 및 이메일 발송 성공!")
    }
    
    fun getMyMatch(partyId: Long, user: User): MyMatchResponse {
        val participant = participantRepository.findByPartyIdAndUser(partyId, user)
            ?: throw IllegalArgumentException("해당 파티에 참가하지 않았습니다.")
        
        val matchResult = matchedResultRepository.findByGiver(participant)
            ?: throw IllegalArgumentException("아직 매칭이 완료되지 않았습니다.")
        
        return MyMatchResponse(receiver = matchResult.receiver.displayName)
    }
}
package party.manitto.domain.match

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.dto.MatchResultResponse
import party.manitto.domain.match.dto.MyMatchResponse
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.entity.MatchedResult
import party.manitto.global.entity.User
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
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
        if (participants.size < 2) {
            throw CustomException(ErrorCode.INVALID_INPUT_VALUE)
        }

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

        // 매칭 결과 이메일 발송 (비동기 처리)
        results.forEach {
            mailService.sendMatchEmailAsync(it.giver.email, it.receiver.displayName, partyName)
        }

        return MatchResultResponse(message = "매칭 완료 및 이메일 발송 중입니다.")
    }
    
    fun getMyMatch(partyId: Long, user: User): MyMatchResponse {
        val participant = participantRepository.findByPartyIdAndUser(partyId, user)
            ?: throw CustomException(ErrorCode.PARTICIPANT_NOT_FOUND)
        
        val matchResult = matchedResultRepository.findByGiver(participant)
            ?: throw CustomException(ErrorCode.RESOURCE_NOT_FOUND)
        
        return MyMatchResponse(receiver = matchResult.receiver.displayName)
    }
}
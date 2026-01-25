package party.manitto.domain.match

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import party.manitto.domain.match.dto.MatchResultResponse
import party.manitto.domain.match.dto.MyMatchResponse
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.entity.EmailJob
import party.manitto.global.entity.EmailJobStatus
import party.manitto.global.entity.MatchedResult
import party.manitto.global.entity.User
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
import kotlin.random.Random

@Service
class MatchService(
    private val participantRepository: ParticipantRepository,
    private val matchedResultRepository: MatchedResultRepository,
    private val emailJobRepository: EmailJobRepository
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

        val savedResults = matchedResultRepository.saveAll(results)

        val jobs = savedResults.map {
            EmailJob(
                matchedResult = it,
                party = it.party,
                toEmail = it.giver.email,
                status = EmailJobStatus.PENDING
            )
        }
        emailJobRepository.saveAll(jobs)

        return MatchResultResponse(message = "매칭 완료. 이메일 발송 대기열에 등록했습니다.")
    }
    
    fun getMyMatch(partyId: Long, user: User): MyMatchResponse {
        val participant = participantRepository.findByPartyIdAndUser(partyId, user)
            ?: throw CustomException(ErrorCode.PARTICIPANT_NOT_FOUND)
        
        val matchResult = matchedResultRepository.findByGiver(participant)
            ?: throw CustomException(ErrorCode.RESOURCE_NOT_FOUND)
        
        return MyMatchResponse(receiver = matchResult.receiver.displayName)
    }
}

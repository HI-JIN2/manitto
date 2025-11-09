package party.manitto.domain.match

import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.entity.Participant
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class MatchService(
    private val participantRepository: ParticipantRepository
) {
    fun matchParticipants(partyId: Long): List<Pair<Participant, Participant>> {
        val participants = participantRepository.findAll()
            .filter { it.party.id == partyId }

        require(participants.size > 1) { "참여자가 2명 이상이어야 합니다." }

        val shuffled = participants.toMutableList()
        do {
            shuffled.shuffle(Random(System.nanoTime()))
        } while (shuffled.zip(participants).any { it.first.id == it.second.id })

        return participants.zip(shuffled)
    }
}
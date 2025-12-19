package party.manitto.domain.participant

import party.manitto.global.entity.Participant

data class ParticipantResponse(
    val id: Long,
    val email: String,
    val displayName: String,
    val nickname: String?,
    val partyId: Long
) {
    companion object {
        fun from(participant: Participant): ParticipantResponse {
            return ParticipantResponse(
                id = participant.id,
                email = participant.email,
                displayName = participant.displayName,
                nickname = participant.nickname,
                partyId = participant.party.id
            )
        }
    }
}
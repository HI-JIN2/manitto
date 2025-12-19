package party.manitto.domain.party

import party.manitto.global.entity.Party
import java.time.LocalDateTime

data class PartyResponse(
    val id: Long,
    val name: String,
    val inviteCode: String,
    val hostEmail: String,
    val hostName: String?,
    val participantCount: Int,
    val isMatched: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(party: Party, isMatched: Boolean = false): PartyResponse {
            return PartyResponse(
                id = party.id,
                name = party.name,
                inviteCode = party.inviteCode,
                hostEmail = party.host.email,
                hostName = party.host.name,
                participantCount = party.participants.size,
                isMatched = isMatched,
                createdAt = party.createdAt
            )
        }
    }
}


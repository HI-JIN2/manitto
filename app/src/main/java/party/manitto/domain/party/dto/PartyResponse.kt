package party.manitto.domain.party.dto

import party.manitto.global.entity.Party
import java.time.LocalDateTime

data class PartyResponse(
    val id: Long,
    val name: String,
    val inviteCode: String,
    val hostEmail: String?,
    val hostName: String?,
    val participantCount: Int,
    val isMatched: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(party: Party, isMatched: Boolean = false): PartyResponse {
            // 게스트 모드인 경우 첫 번째 참가자의 정보 사용
            val hostEmail = party.host?.email ?: party.participants.firstOrNull()?.guestEmail
            val hostName = party.host?.name ?: party.participants.firstOrNull()?.guestName
            
            return PartyResponse(
                id = party.id,
                name = party.name,
                inviteCode = party.inviteCode,
                hostEmail = hostEmail,
                hostName = hostName,
                participantCount = party.participants.size,
                isMatched = isMatched,
                createdAt = party.createdAt
            )
        }
    }
}


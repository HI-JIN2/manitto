package party.manitto.domain.party.dto

data class GuestParticipantRequest(
    val name: String,
    val email: String
)

data class GuestCreatePartyRequest(
    val name: String,
    val hostName: String,
    val hostEmail: String,
    val participants: List<GuestParticipantRequest>? = null
)


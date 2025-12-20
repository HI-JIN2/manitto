package party.manitto.domain.party.dto

data class GuestCreatePartyRequest(
    val name: String,
    val hostName: String,
    val hostEmail: String
)


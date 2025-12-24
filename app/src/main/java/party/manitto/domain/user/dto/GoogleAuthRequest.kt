package party.manitto.domain.user.dto

data class GoogleAuthRequest(
    val credential: String,
    val redirectUri: String? = null
)


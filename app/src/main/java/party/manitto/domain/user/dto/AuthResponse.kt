package party.manitto.domain.user.dto

data class AuthResponse(
    val token: String,
    val error: String? = null
)


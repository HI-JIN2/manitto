package party.manitto.domain.party.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class GuestParticipantRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
    
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String
)

data class GuestCreatePartyRequest(
    @field:NotBlank(message = "파티 이름은 필수입니다.")
    val name: String,
    
    @field:NotBlank(message = "호스트 이름은 필수입니다.")
    val hostName: String,
    
    @field:NotBlank(message = "호스트 이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val hostEmail: String,
    
    @field:Valid
    val participants: List<GuestParticipantRequest>? = null
)


package party.manitto.domain.party.dto

import jakarta.validation.constraints.NotBlank

data class CreatePartyRequest(
    @field:NotBlank(message = "파티 이름은 필수입니다.")
    val name: String
)


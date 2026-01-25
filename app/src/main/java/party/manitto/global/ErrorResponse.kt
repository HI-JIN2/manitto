package party.manitto.global

import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val errors: List<ValidationError>? = null
) {
    data class ValidationError(
        val field: String,
        val value: String?,
        val reason: String?
    )

    companion object {
        fun toResponseEntity(errorCode: ErrorCode): ResponseEntity<ErrorResponse> {
            return ResponseEntity
                .status(errorCode.status)
                .body(
                    ErrorResponse(
                        status = errorCode.status.value(),
                        error = errorCode.status.name,
                        message = errorCode.message
                    )
                )
        }
        
        fun toResponseEntity(errorCode: ErrorCode, message: String): ResponseEntity<ErrorResponse> {
            return ResponseEntity
                .status(errorCode.status)
                .body(
                    ErrorResponse(
                        status = errorCode.status.value(),
                        error = errorCode.status.name,
                        message = message
                    )
                )
        }
    }
}

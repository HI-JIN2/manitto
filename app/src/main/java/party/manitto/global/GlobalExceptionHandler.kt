package party.manitto.global

import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(e.errorCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.map {
            ErrorResponse.ValidationError(
                field = it.field,
                value = it.rejectedValue?.toString(),
                reason = it.defaultMessage
            )
        }
        
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        return ResponseEntity
            .status(errorCode.status)
            .body(
                ErrorResponse(
                    status = errorCode.status.value(),
                    error = errorCode.status.name,
                    message = errorCode.message,
                    errors = errors
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        // 기존 코드와의 호환성을 위해 잠시 유지하거나, 점진적으로 CustomException으로 대체 권장
        // 여기서는 INVALID_INPUT_VALUE로 매핑하되 메시지는 유지
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT_VALUE, ex.message ?: "잘못된 요청입니다.")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        e.printStackTrace() // 실무에서는 로깅 프레임워크 사용
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}
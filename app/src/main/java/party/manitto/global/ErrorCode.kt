package party.manitto.global

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Business
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "파티를 찾을 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참가자를 찾을 수 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),
    ALREADY_MATCHED(HttpStatus.BAD_REQUEST, "이미 매칭이 완료된 파티입니다."),
    PARTY_PARTICIPANT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "파티 참가 인원(30명) 제한을 초과했습니다."),
    ALREADY_JOINED_PARTY(HttpStatus.BAD_REQUEST, "이미 이 파티에 참가한 사용자입니다."),
    ALREADY_JOINED_EMAIL(HttpStatus.BAD_REQUEST, "이미 이 파티에 참가한 이메일입니다."),
    PARTICIPANT_PARTY_MISMATCH(HttpStatus.BAD_REQUEST, "파티 정보가 일치하지 않습니다.")
}

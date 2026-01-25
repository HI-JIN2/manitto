package party.manitto.global

class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)

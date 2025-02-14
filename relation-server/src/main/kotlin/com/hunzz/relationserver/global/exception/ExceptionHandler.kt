package com.hunzz.relationserver.global.exception

import com.hunzz.relationserver.global.exception.custom.InvalidRelationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    // 팔로우 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRelationException::class)
    fun handleInvalidRelationException(e: InvalidRelationException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)
}
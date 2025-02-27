package com.hunzz.api.utility.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    // 시스템 관련 에러
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalSystemException::class)
    fun handleInternalSystemException(e: InternalSystemException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.INTERNAL_SERVER_ERROR)
}
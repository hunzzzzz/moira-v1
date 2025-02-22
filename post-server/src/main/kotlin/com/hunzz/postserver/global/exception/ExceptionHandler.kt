package com.hunzz.postserver.global.exception

import com.hunzz.postserver.global.exception.custom.InternalSystemException
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    // 게시글 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPostInfoException::class)
    fun handleInvalidPostInfoException(e: InvalidPostInfoException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)

    // 시스템 관련 에러
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalSystemException::class)
    fun handleInternalSystemException(e: InternalSystemException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.INTERNAL_SERVER_ERROR)
}
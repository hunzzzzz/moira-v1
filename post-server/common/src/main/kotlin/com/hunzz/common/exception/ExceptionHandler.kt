package com.hunzz.common.exception

import com.hunzz.common.exception.custom.InvalidPostInfoException
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
}
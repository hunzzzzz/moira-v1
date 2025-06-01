package com.hunzz.authserver.utility.exception

import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    // Validation 실패
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException) =
        ErrorResponse(message = e.fieldErrors.first().defaultMessage!!, statusCode = HttpStatus.BAD_REQUEST)

    // 유저 인증 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAuthException::class)
    fun handleInvalidAuthException(e: InvalidAuthException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)
}
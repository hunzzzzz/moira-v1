package com.hunzz.common.exception

import com.hunzz.common.exception.custom.InvalidSignupException
import com.hunzz.common.exception.custom.InvalidUserInfoException
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

    // 회원가입 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSignupException::class)
    fun handleInvalidSignupException(e: InvalidSignupException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)

    // 유저 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUserInfoException::class)
    fun handleInvalidUserInfoException(e: InvalidUserInfoException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)
}
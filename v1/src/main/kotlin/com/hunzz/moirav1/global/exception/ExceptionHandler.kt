package com.hunzz.moirav1.global.exception

import com.hunzz.moirav1.global.exception.custom.InvalidAdminRequestException
import com.hunzz.moirav1.global.exception.custom.InvalidPostInfoException
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
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

    // 유저 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUserInfoException::class)
    fun handleInvalidUserInfoException(e: InvalidUserInfoException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)

    // 게시글 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPostInfoException::class)
    fun handleInvalidPostInfoException(e: InvalidPostInfoException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)

    // 어드민 전용 기능 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAdminRequestException::class)
    fun handleInvalidAdminRequestException(e: InvalidAdminRequestException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.BAD_REQUEST)
}
package com.hunzz.gatewayserver.exception

import com.hunzz.gatewayserver.exception.custom.JwtException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtException::class)
    fun handleJwtException(e: JwtException) =
        ErrorResponse(message = e.message!!, statusCode = HttpStatus.UNAUTHORIZED)
}
package com.hunzz.moirav1.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.global.exception.ErrorMessages.EXPIRED_ATK
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_TOKEN
import com.hunzz.moirav1.global.exception.ErrorMessages.UNPACKED_ATK
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.JwtProvider
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

@WebFilter(filterName = "AuthCheckFilter")
class AuthCheckFilter(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper
) : Filter {
    private fun sendErrorResponse(error: ErrorResponse, response: HttpServletResponse) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(objectMapper.writeValueAsString(error))
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        request as HttpServletRequest
        response as HttpServletResponse

        // check 'Authorization' header
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION) ?: run {
            sendErrorResponse(
                error = ErrorResponse(message = UNPACKED_ATK, statusCode = HttpStatus.BAD_REQUEST),
                response = response
            )
            return
        }

        // substring
        val atk = jwtProvider.substringToken(token = authHeader) ?: run {
            sendErrorResponse(
                error = ErrorResponse(message = INVALID_TOKEN, statusCode = HttpStatus.UNAUTHORIZED),
                response = response
            )
            return
        }

        // validate
        jwtProvider.validateToken(token = atk).onFailure {
            sendErrorResponse(
                error = ErrorResponse(message = EXPIRED_ATK, statusCode = HttpStatus.UNAUTHORIZED),
                response = response
            )
            return
        }

        filterChain.doFilter(request, response)
    }
}
package com.hunzz.authserver.domain.auth.service

import com.hunzz.authserver.domain.auth.dto.request.LoginRequest
import com.hunzz.authserver.domain.auth.dto.response.TokenResponse
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.INVALID_TOKEN
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.JwtProvider
import com.hunzz.common.global.utility.KafkaProducer
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component

@Component
class AuthHandler(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val kafkaProducer: KafkaProducer
) {
    private fun createTokens(userAuth: UserAuth): TokenResponse {
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // send kafka message (redis command)
        kafkaProducer.send(topic = "set-rtk", data = mapOf("email" to userAuth.email, "rtk" to rtk))

        return TokenResponse(atk = atk, rtk = rtk)
    }

    fun login(request: LoginRequest): TokenResponse {
        // validate
        val email = request.email!!
        val password = request.password!!
        val userAuth = authRedisHandler.checkLoginRequest(inputEmail = email, inputPassword = password)

        // create token
        return createTokens(userAuth = userAuth)
    }

    fun logout(email: String, httpServletRequest: HttpServletRequest) {
        // get atk
        val atk = httpServletRequest.getHeader(AUTHORIZATION)

        // send kafka message (redis command)
        kafkaProducer.send(topic = "logout", data = mapOf("email" to email, "atk" to atk))
    }

    fun refresh(httpServletRequest: HttpServletRequest): TokenResponse {
        // get rtk from authorization header
        val authHeader = httpServletRequest.getHeader(AUTHORIZATION)
        val rtk = jwtProvider.substringToken(token = authHeader)

        if (rtk == null || jwtProvider.validateToken(token = rtk).isFailure)
            throw InvalidUserInfoException(INVALID_TOKEN)

        // get email from rtk
        val payload = jwtProvider.getUserInfoFromToken(token = rtk)
        val email = payload.get("email", String::class.java)

        // check rtk & get user auth
        val userAuth = authRedisHandler.checkRtk(email = email, rtkFromAuthHeader = authHeader)

        // create token
        return createTokens(userAuth = userAuth)
    }
}
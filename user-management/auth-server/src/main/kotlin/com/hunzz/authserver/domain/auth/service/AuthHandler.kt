package com.hunzz.authserver.domain.auth.service

import com.hunzz.authserver.domain.auth.dto.request.LoginRequest
import com.hunzz.authserver.domain.auth.dto.response.TokenResponse
import com.hunzz.common.global.utility.KafkaProducer
import org.springframework.stereotype.Component

@Component
class AuthHandler(
    private val authRedisScriptHandler: AuthRedisScriptHandler,
    private val jwtProvider: JwtProvider,
    private val kafkaProducer: KafkaProducer
) {
    fun login(request: LoginRequest): TokenResponse {
        // validate & get user auth
        val email = request.email!!
        val password = request.password!!
        val userAuth = authRedisScriptHandler.checkLoginRequest(inputEmail = email, inputPassword = password)

        // create token
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // send kafka message (redis command)
        kafkaProducer.send(topic = "login", data = mapOf("email" to email, "rtk" to rtk))

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
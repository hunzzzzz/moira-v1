package com.hunzz.authserver.domain.auth.service

import com.hunzz.authserver.domain.auth.dto.request.LoginRequest
import com.hunzz.authserver.domain.auth.dto.response.TokenResponse
import com.hunzz.common.global.utility.RedisCommands
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.stereotype.Component

@Component
class AuthHandler(
    private val authChecker: AuthChecker,
    private val jwtProvider: JwtProvider,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) {
    fun login(request: LoginRequest): TokenResponse {
        // validate & get user auth
        val email = request.email!!
        val password = request.password!!
        val userAuth = authChecker.checkLoginRequest(inputEmail = email, inputPassword = password)

        // create token
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // save rtk in redis
        val rtkKey = redisKeyProvider.rtk(email = request.email!!)
        redisCommands.set(key = rtkKey, value = rtk)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
package com.hunzz.authserver.domain.component

import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.auth.UserAuth
import org.springframework.stereotype.Component

@Component
class TokenHandler(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider
) {
    fun createTokens(userAuth: UserAuth): TokenResponse {
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // Redis에 RTK 저장
        authRedisHandler.setRtk(email = userAuth.email, rtk = rtk)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
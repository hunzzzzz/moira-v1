package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.component.TokenHandler
import com.hunzz.authserver.domain.dto.request.LoginRequest
import com.hunzz.authserver.domain.dto.response.TokenResponse
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val authRedisHandler: AuthRedisHandler,
    private val tokenHandler: TokenHandler
) {
    fun login(request: LoginRequest): TokenResponse {
        // 검증 및 userAuth 객체 획득
        val userAuth = authRedisHandler.validateThenGetUserAuth(
            inputEmail = request.email!!,
            inputPassword = request.password!!
        )

        // 토큰 생성
        return tokenHandler.createTokens(userAuth = userAuth)
    }
}
package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.dto.request.LoginRequest
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.cache.UserCache
import com.hunzz.authserver.utility.client.UserServerClient
import com.hunzz.authserver.utility.exception.ErrorCode.INVALID_LOGIN_INFO
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val userServerClient: UserServerClient
) {
    @UserCache
    fun login(request: LoginRequest): TokenResponse {
        // 이메일 검증
        authRedisHandler.checkValidEmail(email = request.email!!)

        // 비밀번호 일치 여부 확인 + userAuth 객체 획득 → user-data 서버에 위임
        val userAuth = userServerClient.validateThenGetUserAuth(
            email = request.email!!,
            password = request.password!!,
        ) ?: throw InvalidAuthException(INVALID_LOGIN_INFO)

        // 토큰 생성
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        authRedisHandler.setRtk(email = userAuth.email, rtk = rtk)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
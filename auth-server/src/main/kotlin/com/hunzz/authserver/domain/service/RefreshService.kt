package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.client.UserServerClient
import com.hunzz.authserver.utility.exception.ErrorCode.INVALID_TOKEN
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service

@Service
class RefreshService(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val userServerClient: UserServerClient
) {
    fun refresh(httpServletRequest: HttpServletRequest): TokenResponse {
        // 'Authorization' 헤더에서 RTK 추출
        val authHeader = httpServletRequest.getHeader(AUTHORIZATION)
        val rtk = jwtProvider.substringToken(token = authHeader)

        // RTK 1차 검증
        if (rtk == null || jwtProvider.validateToken(token = rtk).isFailure)
            throw InvalidAuthException(INVALID_TOKEN)

        // RTK에서 이메일 추출
        val payload = jwtProvider.getUserInfoFromToken(token = rtk)
        val email = payload.get("email", String::class.java)

        // RTK 2차 검증
        authRedisHandler.validateRtk(email = email, rtkFromAuthHeader = authHeader)

        // 토큰 재생성
        val userAuth = userServerClient.getUserAuth(email = email)
        val newAtk = jwtProvider.createAccessToken(userAuth = userAuth)
        val newRtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        return TokenResponse(atk = newAtk, rtk = newRtk)
    }
}
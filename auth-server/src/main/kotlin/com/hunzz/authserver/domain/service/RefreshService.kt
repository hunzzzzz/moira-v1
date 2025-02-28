package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.component.TokenHandler
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.exception.ErrorCode.INVALID_TOKEN
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service

@Service
class RefreshService(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val tokenHandler: TokenHandler,
) {
    fun refresh(httpServletRequest: HttpServletRequest): TokenResponse {
        // 'Authorization' 헤더에서 RTK 추출
        val authHeader = httpServletRequest.getHeader(AUTHORIZATION)
        val rtk = jwtProvider.substringToken(token = authHeader)

        // RTK 검증
        if (rtk == null || jwtProvider.validateToken(token = rtk).isFailure)
            throw InvalidAuthException(INVALID_TOKEN)

        // RTK에서 이메일 추출
        val payload = jwtProvider.getUserInfoFromToken(token = rtk)
        val email = payload.get("email", String::class.java)

        // Authorization 헤더로 넘어온 RTK와, Redis에 있는 RTK를 비교
        val userAuth = authRedisHandler.checkRtkThenGetUserAuth(email = email, rtkFromAuthHeader = authHeader)

        // create token
        return tokenHandler.createTokens(userAuth = userAuth)
    }
}
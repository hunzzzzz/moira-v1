package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.utility.auth.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider
) {
    fun logout(httpServletRequest: HttpServletRequest) {
        // Authorization 헤더에서 ATK 추출
        val authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        val atk = jwtProvider.substringToken(token = authHeader)

        // ATK에서 이메일 추출
        val payload = jwtProvider.getUserInfoFromToken(token = atk!!)
        val email = payload.get("email", String::class.java)

        // Redis에서 ATK 차단 및 RTK 삭제
        authRedisHandler.blockAtkThenDeleteRtk(atk = atk, email = email)
    }
}
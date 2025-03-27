package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthKafkaHandler
import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.component.OauthRequestSender
import com.hunzz.authserver.domain.component.UserAuthProvider
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.cache.UserCache
import org.springframework.stereotype.Service

@Service
class KakaoLoginService(
    private val authKafkaHandler: AuthKafkaHandler,
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val oauthRequestSender: OauthRequestSender,
    private val userAuthProvider: UserAuthProvider
) {
    @UserCache
    suspend fun kakaoLogin(code: String): TokenResponse {
        // 카카오 접근용 토큰 획득
        val kakaoToken = oauthRequestSender.getKakaoToken(code = code)

        // 유저 정보 획득
        val userInfo = oauthRequestSender.getKakaoUserInfo(accessToken = kakaoToken.accessToken)
        val email = userInfo.kakaoAccount.email

        // UserAuth 객체 획득
        val isNewcomer = authRedisHandler.isNewcomer(email = email)
        val userAuth = if (isNewcomer) {
            // 신규회원인 경우, 회원가입 진행
            val userId = authKafkaHandler.kakaoUserSignup(
                email = email,
                name = userInfo.kakaoAccount.profile.nickname
            )
            UserAuth(userId, "KAKAO", "USER", email)
        } else userAuthProvider.getUserAuth(email = email)

        // 서비스용 토큰 생성
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
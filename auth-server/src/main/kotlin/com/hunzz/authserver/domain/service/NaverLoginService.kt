package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthKafkaHandler
import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.component.OauthRequestSender
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.cache.UserCache
import com.hunzz.authserver.utility.client.UserServerClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class NaverLoginService(
    private val authKafkaHandler: AuthKafkaHandler,
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val oauthRequestSender: OauthRequestSender,
    private val userServerClient: UserServerClient
) {
    @UserCache
    suspend fun naverLogin(code: String): TokenResponse {
        // 네이버 접근용 토큰 획득
        val naverToken = oauthRequestSender.getNaverToken(code = code)

        // 유저 정보 획득
        val userInfo = oauthRequestSender.getNaverUserInfo(accessToken = naverToken.accessToken)
        val email = userInfo.naverProfile.email

        // UserAuth 객체 획득
        val isNewcomer = authRedisHandler.isNewcomer(email = email)
        val userAuth = if (isNewcomer) {
            // 신규회원인 경우, 회원가입 진행
            val userId = authKafkaHandler.kakaoUserSignup(
                email = email,
                name = userInfo.naverProfile.name
            )
            UserAuth(userId, "NAVER", "USER", email)
        } else withContext(Dispatchers.IO) {
            userServerClient.getUserAuth(email = email)
        }

        // 서비스용 토큰 생성
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}
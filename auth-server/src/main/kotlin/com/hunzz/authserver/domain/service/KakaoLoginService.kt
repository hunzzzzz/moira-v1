package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.AuthCacheManager
import com.hunzz.authserver.domain.component.AuthKafkaHandler
import com.hunzz.authserver.domain.component.AuthRedisHandler
import com.hunzz.authserver.domain.component.OauthRequestSender
import com.hunzz.authserver.domain.dto.response.KakaoTokenResponse
import com.hunzz.authserver.domain.dto.response.kakao.KakaoUserInfo
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.cache.UserCache
import org.springframework.stereotype.Service

@Service
class KakaoLoginService(
    private val authCacheManager: AuthCacheManager,
    private val authKafkaHandler: AuthKafkaHandler,
    private val authRedisHandler: AuthRedisHandler,
    private val jwtProvider: JwtProvider,
    private val oauthRequestSender: OauthRequestSender
) {
    private suspend fun checkIsNewcomerThenGetUserAuth(userInfo: KakaoUserInfo): UserAuth {
        val email = userInfo.kakaoAccount.email
        val name = userInfo.kakaoAccount.profile.nickname

        // 신규 회원인 경우, 회원가입 진행
        return if (authRedisHandler.isNewcomer(email)) {
            // Kafka 메시지 전송 (auth -> user-api)
            val userId = authKafkaHandler.kakaoUserSignup(
                email = email,
                name = name
            )
            UserAuth(userId, "KAKAO", "USER", email, "")
        } else
            authCacheManager.getUserAuthWithLocalCache(email)
    }

    @UserCache
    suspend fun kakaoLogin(code: String): KakaoTokenResponse {
        // 카카오 접근용 토큰 획득
        val kakaoToken = oauthRequestSender.getKakaoToken(code = code)

        // 유저 정보 획득
        val userInfo = oauthRequestSender.getKakaoUserInfo(accessToken = kakaoToken.accessToken)

        // 유저 인증 정보 획득
        val userAuth = checkIsNewcomerThenGetUserAuth(userInfo = userInfo)

        // 서비스용 토큰 생성
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        return KakaoTokenResponse.from(atk = atk, rtk = rtk, kakaoToken = kakaoToken)
    }
}
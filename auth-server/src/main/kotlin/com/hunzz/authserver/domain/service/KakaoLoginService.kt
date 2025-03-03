package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.*
import com.hunzz.authserver.domain.dto.response.KakaoTokenResponse
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.cache.UserCache
import com.hunzz.authserver.utility.exception.ErrorCode.OAUTH_LOGIN_ERROR
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import org.springframework.stereotype.Service

@Service
class KakaoLoginService(
    private val authCacheManager: AuthCacheManager,
    private val authKafkaHandler: AuthKafkaHandler,
    private val authRedisHandler: AuthRedisHandler,
    private val oauthRequestSender: OauthRequestSender,
    private val tokenHandler: TokenHandler
) {
    @UserCache
    suspend fun login(code: String): KakaoTokenResponse {
        // 카카오 접근용 토큰 획득
        val kakaoToken = oauthRequestSender.getKakaoToken(code = code)

        // 유저 정보 획득
        val userInfo = oauthRequestSender.getKakaoUserInfo(accessToken = kakaoToken.accessToken)
        val email = userInfo.kakaoAccount.email ?: throw InvalidAuthException(OAUTH_LOGIN_ERROR)

        // 신규 회원인 경우, 회원가입 진행
        val isNewcomer = authRedisHandler.isNewcomer(email = email)
        val userAuth = if (isNewcomer) {
            val userId = authKafkaHandler.kakaoUserSignup(email = email, name = userInfo.kakaoAccount.profile.nickname)

            UserAuth(userId = userId, type = "KAKAO", role = "USER", email = email, password = "")
        } else authCacheManager.getUserAuthWithLocalCache(email = email)

        // 서비스용 토큰 생성
        val tokens = tokenHandler.createTokens(userAuth = userAuth)

        return KakaoTokenResponse.from(atk = tokens.atk, rtk = tokens.rtk, kakaoToken = kakaoToken)
    }
}
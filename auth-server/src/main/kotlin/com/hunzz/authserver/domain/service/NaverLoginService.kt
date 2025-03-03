package com.hunzz.authserver.domain.service

import com.hunzz.authserver.domain.component.*
import com.hunzz.authserver.domain.dto.response.NaverTokenResponse
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.cache.UserCache
import org.springframework.stereotype.Service

@Service
class NaverLoginService(
    private val authCacheManager: AuthCacheManager,
    private val authKafkaHandler: AuthKafkaHandler,
    private val authRedisHandler: AuthRedisHandler,
    private val oauthRequestSender: OauthRequestSender,
    private val tokenHandler: TokenHandler
) {
    @UserCache
    suspend fun login(code: String): NaverTokenResponse {
        // 네이버 접근용 토큰 획득
        val naverToken = oauthRequestSender.getNaverToken(code = code)

        // 유저 정보 획득
        val userInfo = oauthRequestSender.getNaverUserInfo(accessToken = naverToken.accessToken)
        val email = userInfo.naverProfile.email
        val isNewcomer = authRedisHandler.isNewcomer(email = email)

        // 신규 회원인 경우, 회원가입 진행
        val userAuth = if (isNewcomer) {
            val userId = authKafkaHandler.naverUserSignup(email = email, name = userInfo.naverProfile.name)

            UserAuth(userId = userId, type = "NAVER", role = "USER", email = email, password = "")
        } else authCacheManager.getUserAuthWithLocalCache(email = email)

        // 서비스용 토큰 생성
        val tokens = tokenHandler.createTokens(userAuth = userAuth)

        return NaverTokenResponse.from(atk = tokens.atk, rtk = tokens.rtk, naverToken = naverToken)
    }
}
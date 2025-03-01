package com.hunzz.authserver.domain.component

import com.hunzz.authserver.domain.dto.response.kakao.KakaoToken
import com.hunzz.authserver.domain.dto.response.kakao.KakaoUserInfo
import com.hunzz.authserver.utility.exception.ErrorCode.OAUTH_LOGIN_ERROR
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration

@Component
class OauthRequestSender(
    @Value("\${kakao.client_id}")
    private val clientId: String,
    @Value("\${kakao.redirect_uri}")
    private val redirectUri: String,

    webClientBuilder: WebClient.Builder
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // 카카오 전용 WebClient 인스턴스 생성
    private val kakaoAuthClient = webClientBuilder
        .baseUrl("https://kauth.kakao.com")
        .build()

    private val kakaoApiClient = webClientBuilder
        .baseUrl("https://kapi.kakao.com")
        .build()

    suspend fun getKakaoToken(
        code: String
    ): KakaoToken {
        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("redirect_uri", redirectUri)
            add("code", code)
        }

        return kakaoAuthClient
            .post()
            .uri("/oauth/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(KakaoToken::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // 1초 간격, 3회 재시도
            .doOnError { logger.error("[에러] 카카오 토큰 요청 과정에서 에러가 발생하였습니다.", it) }
            .onErrorMap { InvalidAuthException(OAUTH_LOGIN_ERROR) }
            .awaitSingle()
    }

    suspend fun getKakaoUserInfo(
        accessToken: String
    ): KakaoUserInfo {
        return kakaoApiClient
            .post()
            .uri("/v2/user/me?secure_resource=true")
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.toString())
            .retrieve()
            .bodyToMono(KakaoUserInfo::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // 1초 간격, 3회 재시도
            .doOnError { logger.error("[에러] 카카오 유저 정보 획득 과정에서 에러가 발생하였습니다.", it) }
            .onErrorMap { InvalidAuthException(OAUTH_LOGIN_ERROR) }
            .awaitSingle()
    }
}
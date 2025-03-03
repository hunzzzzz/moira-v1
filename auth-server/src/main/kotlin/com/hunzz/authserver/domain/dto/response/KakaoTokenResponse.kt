package com.hunzz.authserver.domain.dto.response

import com.hunzz.authserver.domain.dto.response.kakao.KakaoToken

data class KakaoTokenResponse(
    override val atk: String,
    override val rtk: String,
    val kakaoToken: KakaoToken
) : TokenResponse(atk = atk, rtk = rtk) {
    companion object {
        fun from(atk: String, rtk: String, kakaoToken: KakaoToken) = KakaoTokenResponse(
            atk = atk,
            rtk = rtk,
            kakaoToken = kakaoToken
        )
    }
}

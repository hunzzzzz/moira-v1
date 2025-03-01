package com.hunzz.authserver.domain.dto.response

import com.hunzz.authserver.domain.dto.response.kakao.KakaoToken

data class KakaoTokenResponse(
    val atk: String,
    val rtk: String,
    val kakaoToken: KakaoToken
) {
    companion object {
        fun from(atk: String, rtk: String, kakaoToken: KakaoToken) = KakaoTokenResponse(
            atk = atk,
            rtk = rtk,
            kakaoToken = kakaoToken
        )
    }
}

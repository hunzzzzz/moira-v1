package com.hunzz.authserver.domain.dto.response

import com.hunzz.authserver.domain.dto.response.naver.NaverToken

data class NaverTokenResponse(
    override val atk: String,
    override val rtk: String,
    val naverToken: NaverToken
) : TokenResponse(atk = atk, rtk = rtk) {
    companion object {
        fun from(atk: String, rtk: String, naverToken: NaverToken) = NaverTokenResponse(
            atk = atk,
            rtk = rtk,
            naverToken = naverToken
        )
    }
}

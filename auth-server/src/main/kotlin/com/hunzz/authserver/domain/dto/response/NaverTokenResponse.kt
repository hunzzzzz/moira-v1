package com.hunzz.authserver.domain.dto.response

import com.hunzz.authserver.domain.dto.response.naver.NaverToken

data class NaverTokenResponse(
    val atk: String,
    val rtk: String,
    val naverToken: NaverToken
) {
    companion object {
        fun from(atk: String, rtk: String, naverToken: NaverToken) = NaverTokenResponse(
            atk = atk,
            rtk = rtk,
            naverToken = naverToken
        )
    }
}

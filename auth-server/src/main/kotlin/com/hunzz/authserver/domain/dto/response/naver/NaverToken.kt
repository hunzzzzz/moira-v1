package com.hunzz.authserver.domain.dto.response.naver

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverToken(
    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("refresh_token")
    val refreshToken: String
)

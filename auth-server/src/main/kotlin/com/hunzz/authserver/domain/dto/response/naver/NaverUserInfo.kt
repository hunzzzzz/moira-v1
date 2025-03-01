package com.hunzz.authserver.domain.dto.response.naver

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverUserInfo(
    @JsonProperty(value = "response")
    val naverProfile: NaverProfile
)

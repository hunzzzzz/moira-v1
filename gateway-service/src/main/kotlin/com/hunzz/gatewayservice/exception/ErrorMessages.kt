package com.hunzz.gatewayservice.exception

object ErrorMessages {
    const val INVALID_TOKEN = "유효하지 않은 JWT 토큰입니다."
    const val UNPACKED_ATK = "요청에 Authorization 헤더가 포함되지 않았습니다."
    const val EXPIRED_ATK = "Access Token이 만료되었습니다."
    const val EXPIRED_AUTH = "유저 정보가 만료되었습니다. 다시 로그인해주세요."
}
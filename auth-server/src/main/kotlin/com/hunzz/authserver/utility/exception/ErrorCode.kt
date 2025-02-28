package com.hunzz.authserver.utility.exception

enum class ErrorCode(val message: String) {
    // auth
    INVALID_LOGIN_INFO("이메일 혹은 비밀번호가 잘못되었습니다. 다시 확인해주세요."),
    EXPIRED_AUTH("사용자 정보가 만료되었습니다. 다시 로그인해주세요."),
    INVALID_TOKEN("유효하지 않은 JWT 토큰입니다.")
}
package com.hunzz.common.exception

enum class ErrorCode(val message: String) {
    // signup
    EXPIRED_SIGNUP_CODE("본인인증 코드가 만료되었습니다. 다시 시도해주세요."),
    INVALID_SIGNUP_CODE("본인인증 코드가 일치하지 않습니다."),

    // user
    DIFFERENT_TWO_PASSWORDS("두 비밀번호가 일치하지 않습니다."),
    DUPLICATED_EMAIL("이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND("존재하지 않는 유저입니다."),

    INVALID_IMAGE_FILE("유효하지 않는 이미지 파일입니다."),
    INVALID_IMAGE_EXTENSION("지원하지 않는 확장자입니다."),

    // auth
    INVALID_LOGIN_INFO("이메일 혹은 비밀번호가 잘못되었습니다. 다시 확인해주세요."),
    BANNED_USER_CANNOT_LOGIN("계정이 정지되어 로그인이 불가능합니다."),
    INVALID_TOKEN("유효하지 않은 JWT 토큰입니다."),

    // admin
    INVALID_ADMIN_CODE("잘못된 어드민 가입 코드입니다."),

    // system
    IMAGE_SYSTEM_ERROR("이미지 시스템이 에러가 발생하였습니다. 잠시 후에 다시 시도해주세요.")
}
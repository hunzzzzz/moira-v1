package com.hunzz.common.global.exception

enum class ErrorCode(val message: String) {
    // signup
    UNWRITTEN_EMAIL("이메일은 필수 입력 항목입니다."),
    UNWRITTEN_PASSWORD("비밀번호는 필수 입력 항목입니다."),
    UNWRITTEN_PASSWORD2("한 번 더 비밀번호를 입력해주세요."),
    UNWRITTEN_NAME("이름은 필수 입력 항목입니다."),

    INVALID_EMAIL("올바르지 않은 이메일 형식입니다."),
    INVALID_PASSWORD("올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)"),

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
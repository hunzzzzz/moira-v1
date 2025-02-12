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

    // admin
    INVALID_ADMIN_CODE("잘못된 어드민 가입 코드입니다.")
}
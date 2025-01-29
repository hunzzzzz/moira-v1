package com.hunzz.moirav1.global.exception

object ErrorMessages {
    // signup
    const val UNWRITTEN_EMAIL = "이메일은 필수 입력 항목입니다."
    const val UNWRITTEN_PASSWORD = "비밀번호는 필수 입력 항목입니다."
    const val UNWRITTEN_PASSWORD2 = "한 번 더 비밀번호를 입력해주세요."
    const val UNWRITTEN_NAME = "이름은 필수 입력 항목입니다."

    const val INVALID_EMAIL = "올바르지 않은 이메일 형식입니다."
    const val INVALID_PASSWORD = "올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)"

    const val DIFFERENT_TWO_PASSWORDS = "두 비밀번호가 일치하지 않습니다."
    const val DUPLICATED_EMAIL = "이미 사용 중인 이메일입니다."

    // login
    const val INVALID_LOGIN_INFO = "이메일 혹은 비밀번호가 잘못되었습니다. 다시 확인해주세요."
    const val BANNED_USER_CANNOT_LOGIN = "계정이 정지되어 로그인이 불가능합니다."

    // admin
    const val INVALID_ADMIN_CODE = "잘못된 어드민 가입 코드입니다."

    // user
    const val USER_NOT_FOUND = "존재하지 않는 유저입니다."
}
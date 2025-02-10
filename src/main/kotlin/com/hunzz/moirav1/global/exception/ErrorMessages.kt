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

    // auth
    const val INVALID_TOKEN = "유효하지 않은 JWT 토큰입니다."
    const val UNPACKED_ATK = "요청에 Authorization 헤더가 포함되지 않았습니다."
    const val EXPIRED_ATK = "Access Token이 만료되었습니다."
    const val EXPIRED_AUTH = "유저 정보가 만료되었습니다. 다시 로그인해주세요."

    // admin
    const val INVALID_ADMIN_CODE = "잘못된 어드민 가입 코드입니다."

    // user
    const val USER_NOT_FOUND = "존재하지 않는 유저입니다."

    // relation
    const val CANNOT_FOLLOW_ITSELF = "자신을 팔로우할 수 없습니다."
    const val CANNOT_UNFOLLOW_ITSELF = "자신을 언팔로우할 수 없습니다."
    const val ALREADY_FOLLOWED = "이미 팔로우한 대상입니다."
    const val ALREADY_UNFOLLOWED = "잘못된 요청입니다."

    // post
    const val UNWRITTEN_POST_CONTENT = "게시글 내용은 필수 입력 항목입니다."
    const val UNWRITTEN_SCOPE = "게시글 공개 범위는 필수 입력 항목입니다."
    const val INVALID_POST_CONTENT = "게시글은 1자 이상, 500자 이하로 작성해주세요."

    const val POST_NOT_FOUND = "존재하지 않는 게시글입니다."
    const val CANNOT_UPDATE_OTHERS_POST = "본인의 게시글이 아닙니다."
}
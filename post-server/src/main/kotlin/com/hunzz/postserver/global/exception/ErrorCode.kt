package com.hunzz.postserver.global.exception

enum class ErrorCode(val message: String) {
    // post
    UNWRITTEN_POST_CONTENT("게시글 내용은 필수 입력 항목입니다."),
    UNWRITTEN_SCOPE("게시글 공개 범위는 필수 입력 항목입니다."),
    INVALID_POST_CONTENT("게시글은 1자 이상, 500자 이하로 작성해주세요."),

    POST_NOT_FOUND("존재하지 않는 게시글입니다."),
    CANNOT_UPDATE_OTHERS_POST("본인의 게시글이 아닙니다.")
}
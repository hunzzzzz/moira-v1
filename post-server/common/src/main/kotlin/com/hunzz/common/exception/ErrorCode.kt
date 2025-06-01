package com.hunzz.common.exception

enum class ErrorCode(val message: String) {
    // post
    POST_NOT_FOUND("존재하지 않는 게시글입니다."),
    CANNOT_UPDATE_OTHERS_POST("본인의 게시글이 아닙니다."),
    ALREADY_LIKED("이미 좋아요한 게시글입니다."),
    ALREADY_UNLIKED("잘못된 요청입니다."),

    MAX_IMAGE_COUNT_EXCEEDED("이미지는 최대 10개까지 업로드할 수 있습니다."),
    INVALID_IMAGE_FILE("유효하지 않은 이미지 파일입니다.")
}
package com.hunzz.postserver.global.exception

enum class ErrorCode(val message: String) {
    // post
    POST_NOT_FOUND("존재하지 않는 게시글입니다."),
    CANNOT_UPDATE_OTHERS_POST("본인의 게시글이 아닙니다."),
    ALREADY_LIKED("이미 좋아요한 게시글입니다."),
    ALREADY_UNLIKED("잘못된 요청입니다."),

    INVALID_IMAGE_FILE("유효하지 않는 이미지 파일입니다."),
    INVALID_IMAGE_EXTENSION("지원하지 않는 확장자입니다."),

    // comment
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),
    CANNOT_UPDATE_OTHERS_COMMENT("본인의 댓글이 아닙니다."),
    COMMENT_NOT_BELONGS_TO_POST("해당 게시글에 소속되지 않은 댓글입니다."),

    // system
    IMAGE_SYSTEM_ERROR("이미지 시스템이 에러가 발생하였습니다. 잠시 후에 다시 시도해주세요.")
}
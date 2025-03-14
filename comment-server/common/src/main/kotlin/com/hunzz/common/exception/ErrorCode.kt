package com.hunzz.common.exception

enum class ErrorCode(val message: String) {
    // comment
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),
    CANNOT_UPDATE_OTHERS_COMMENT("본인의 댓글이 아닙니다."),
    COMMENT_NOT_BELONGS_TO_POST("해당 게시글에 소속되지 않은 댓글입니다."),
}
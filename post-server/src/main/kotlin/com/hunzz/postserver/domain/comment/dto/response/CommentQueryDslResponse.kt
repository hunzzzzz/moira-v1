package com.hunzz.postserver.domain.comment.dto.response

import com.hunzz.postserver.domain.comment.model.CommentStatus

data class CommentQueryDslResponse(
    val commentId: Long,
    val status: CommentStatus,
    val content: String
)
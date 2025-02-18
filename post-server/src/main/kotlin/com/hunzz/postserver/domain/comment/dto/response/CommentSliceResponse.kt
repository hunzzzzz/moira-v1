package com.hunzz.postserver.domain.comment.dto.response

data class CommentSliceResponse(
    val currentCursor: Long?,
    val nextCursor: Long?,
    val contents: List<CommentResponse?>
)
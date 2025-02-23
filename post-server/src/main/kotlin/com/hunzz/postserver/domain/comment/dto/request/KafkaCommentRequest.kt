package com.hunzz.postserver.domain.comment.dto.request

import java.util.UUID

data class KafkaCommentRequest(
    var postAuthorId: UUID,
    val commentAuthorId: UUID,
    val postId: Long,
    val commentId: Long
)

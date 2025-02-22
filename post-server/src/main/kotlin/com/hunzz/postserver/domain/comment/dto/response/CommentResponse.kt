package com.hunzz.postserver.domain.comment.dto.response

import com.hunzz.postserver.domain.comment.model.CommentStatus
import java.util.*

data class CommentResponse(
    val commentId: Long,
    val status: CommentStatus,
    val content: String,
    val userId: UUID,
    val userName: String,
    val userImageUrl: String?,
    val userThumbnailUrl: String?
)
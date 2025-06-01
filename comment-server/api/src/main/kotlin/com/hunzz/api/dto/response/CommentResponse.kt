package com.hunzz.api.dto.response

import java.time.LocalDateTime
import java.util.*

data class CommentResponse(
    val commentId: UUID,
    val commentContent: String,
    val commentCreatedAt: LocalDateTime,
    val userId: UUID,
    val userName: String,
    val userThumbnailUrl: String?,
    val isMyComment: Boolean,
    val isPostAuthor: Boolean
)

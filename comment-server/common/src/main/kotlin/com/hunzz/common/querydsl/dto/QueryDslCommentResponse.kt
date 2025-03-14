package com.hunzz.common.querydsl.dto

import java.time.LocalDateTime
import java.util.*

data class QueryDslCommentResponse(
    val commentId: UUID,
    val content: String,
    val createdAt: LocalDateTime,
    val userId: UUID
)

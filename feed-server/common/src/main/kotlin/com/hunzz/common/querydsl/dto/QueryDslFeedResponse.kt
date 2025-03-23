package com.hunzz.common.querydsl.dto

import java.util.*

data class QueryDslFeedResponse(
    val feedId: Long,
    val postId: UUID,
    val authorId: UUID
)

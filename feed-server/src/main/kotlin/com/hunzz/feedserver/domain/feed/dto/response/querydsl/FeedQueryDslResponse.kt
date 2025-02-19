package com.hunzz.feedserver.domain.feed.dto.response.querydsl

import java.util.*

data class FeedQueryDslResponse(
    val feedId: Long,
    val userId: UUID,
    val postId: Long,
    val authorId: UUID
)

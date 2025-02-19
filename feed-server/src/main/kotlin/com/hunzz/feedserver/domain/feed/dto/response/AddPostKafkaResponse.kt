package com.hunzz.feedserver.domain.feed.dto.response

import java.util.*

data class AddPostKafkaResponse(
    val authorId: UUID,
    val postId: Long
)

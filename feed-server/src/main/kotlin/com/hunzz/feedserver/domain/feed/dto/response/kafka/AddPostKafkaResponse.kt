package com.hunzz.feedserver.domain.feed.dto.response.kafka

import java.util.*

data class AddPostKafkaResponse(
    val authorId: UUID,
    val postId: Long
)

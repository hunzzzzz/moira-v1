package com.hunzz.postserver.domain.post.dto.request

import java.util.*

data class KafkaPostRequest(
    val authorId: UUID,
    val postId: Long
)

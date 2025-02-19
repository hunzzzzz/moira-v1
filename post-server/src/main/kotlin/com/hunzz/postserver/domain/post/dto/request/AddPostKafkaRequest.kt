package com.hunzz.postserver.domain.post.dto.request

import java.util.*

data class AddPostKafkaRequest(
    val authorId: UUID,
    val postId: Long
)

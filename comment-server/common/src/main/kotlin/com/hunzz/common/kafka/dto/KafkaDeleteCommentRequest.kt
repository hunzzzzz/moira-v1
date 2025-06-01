package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaDeleteCommentRequest(
    val commentId: UUID,
    val userId: UUID,
    val postId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
}
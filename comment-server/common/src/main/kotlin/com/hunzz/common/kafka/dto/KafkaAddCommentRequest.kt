package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaAddCommentRequest(
    val userId: UUID,
    val postId: UUID,
    val commentId: UUID,
    val content: String
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "")
}
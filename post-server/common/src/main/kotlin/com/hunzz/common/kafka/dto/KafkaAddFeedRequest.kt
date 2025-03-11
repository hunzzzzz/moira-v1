package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaAddFeedRequest(
    val authorId: UUID,
    val postId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
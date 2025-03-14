package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaPostAuthorCacheRequest(
    val postId: UUID,
    val authorId: UUID
) {
    constructor(): this(UUID.randomUUID(), UUID.randomUUID())
}
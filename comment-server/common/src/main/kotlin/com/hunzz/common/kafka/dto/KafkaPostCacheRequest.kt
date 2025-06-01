package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaPostCacheRequest(
    val postId: UUID
) {
    constructor() : this(UUID.randomUUID())
}
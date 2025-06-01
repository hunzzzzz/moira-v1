package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaDeletePostRequest(
    val postId: UUID,
    val userId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}

package com.hunzz.consumer.dto

import java.util.*

data class KafkaUpdateFeedWhenFollowRequest(
    val userId: UUID,
    val authorId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
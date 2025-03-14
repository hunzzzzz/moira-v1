package com.hunzz.consumer.dto

import java.util.*

data class KafkaUpdateFeedWhenAddPostRequest(
    val authorId: UUID,
    val postId: UUID,
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
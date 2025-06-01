package com.hunzz.data.dto

import java.util.*

data class FeedQueueDto(
    val userId: UUID,
    val authorId: UUID,
    val postId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
}
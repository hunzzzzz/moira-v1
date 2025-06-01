package com.hunzz.data.dto

import java.util.*

data class FeedDeleteQueueDto(
    val userId: UUID,
    val authorId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
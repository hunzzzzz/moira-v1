package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaUpdateUserImageUrlsRequest(
    val userId: UUID,
    val originalUrl: String,
    val thumbnailUrl: String
) {
    constructor() : this(UUID.randomUUID(), "", "")
}
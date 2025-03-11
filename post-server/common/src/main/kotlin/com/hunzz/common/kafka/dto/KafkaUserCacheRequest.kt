package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaUserCacheRequest(
    val userId: UUID
) {
    constructor() : this(UUID.randomUUID())
}

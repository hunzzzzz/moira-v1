package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaAddUserCacheRequest(
    val userId: UUID
) {
    constructor() : this(UUID.randomUUID())
}
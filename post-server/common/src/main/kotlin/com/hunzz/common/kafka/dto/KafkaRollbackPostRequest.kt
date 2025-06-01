package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaRollbackPostRequest(
    val txId: UUID
) {
    constructor() : this(UUID.randomUUID())
}

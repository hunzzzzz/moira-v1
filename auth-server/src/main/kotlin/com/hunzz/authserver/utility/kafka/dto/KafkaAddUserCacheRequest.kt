package com.hunzz.authserver.utility.kafka.dto

import java.util.*

data class KafkaAddUserCacheRequest(
    val userId: UUID
)

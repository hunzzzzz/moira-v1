package com.hunzz.relationserver.utility.kafka.dto

import java.util.UUID

data class KafkaUpdateFeedRequest(
    val userId: UUID,
    val authorId: UUID
)

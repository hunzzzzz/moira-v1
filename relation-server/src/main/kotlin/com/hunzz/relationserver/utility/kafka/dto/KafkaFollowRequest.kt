package com.hunzz.relationserver.utility.kafka.dto

import java.util.UUID

data class KafkaFollowRequest(
    val userId: UUID,
    val targetId: UUID
)

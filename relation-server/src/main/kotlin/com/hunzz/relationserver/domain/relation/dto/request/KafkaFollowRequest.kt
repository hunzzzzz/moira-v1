package com.hunzz.relationserver.domain.relation.dto.request

import java.util.UUID

data class KafkaFollowRequest(
    val userId: UUID,
    val targetId: UUID
)

package com.hunzz.feedserver.domain.feed.dto.response

import java.util.UUID

data class FollowKafkaResponse(
    val userId: UUID,
    val targetId: UUID
)

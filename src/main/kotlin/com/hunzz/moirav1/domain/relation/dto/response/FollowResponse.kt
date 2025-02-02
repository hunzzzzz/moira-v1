package com.hunzz.moirav1.domain.relation.dto.response

import java.time.LocalDateTime
import java.util.*

data class FollowResponse(
    val userId: UUID,
    val name: String,
    val imageUrl: String?,
    val createdAt: LocalDateTime
)
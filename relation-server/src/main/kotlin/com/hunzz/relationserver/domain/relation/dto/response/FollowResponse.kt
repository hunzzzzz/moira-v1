package com.hunzz.relationserver.domain.relation.dto.response

import java.util.*

data class FollowResponse(
    val userId: UUID,
    val status: String,
    val name: String,
    val imageUrl: String?
)
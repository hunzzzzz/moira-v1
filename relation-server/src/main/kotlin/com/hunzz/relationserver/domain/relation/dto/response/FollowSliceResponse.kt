package com.hunzz.relationserver.domain.relation.dto.response

import java.util.*

data class FollowSliceResponse(
    val currentCursor: UUID?,
    val nextCursor: UUID?,
    val contents: List<FollowResponse>
)

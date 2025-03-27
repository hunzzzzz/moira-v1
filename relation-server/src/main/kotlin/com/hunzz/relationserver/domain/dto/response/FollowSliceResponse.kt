package com.hunzz.relationserver.domain.dto.response

import java.util.*

data class FollowSliceResponse(
    val nextCursor: UUID?,
    val contents: List<FollowResponse?>
)

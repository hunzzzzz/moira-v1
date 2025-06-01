package com.hunzz.api.dto.response

import java.util.*

data class CommentSliceResponse(
    val currentCursor: UUID?,
    val nextCursor: UUID?,
    val contents: List<CommentResponse>
)

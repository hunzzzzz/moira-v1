package com.hunzz.feedserver.domain.feed.dto.response

data class FeedSliceResponse(
    val currentCursor: Long?,
    val nextCursor: Long?,
    val contents: List<FeedResponse>
)
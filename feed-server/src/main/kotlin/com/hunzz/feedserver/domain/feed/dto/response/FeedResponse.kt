package com.hunzz.feedserver.domain.feed.dto.response

import java.util.*

data class FeedResponse(
    val postId: Long,
    val postScope: String,
    val postStatus: String,
    val postContent: String,
    val userId: UUID,
    val userName: String,
    val userImageUrl: String?,
    val numOfLikes: Long,
    val hasLike: Boolean
)

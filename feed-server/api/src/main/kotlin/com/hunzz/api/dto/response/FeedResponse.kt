package com.hunzz.api.dto.response

import java.util.*

data class FeedResponse(
    // feed id
    val feedId: Long,
    // post 관련
    val postId: UUID,
    val postScope: String,
    val postStatus: String,
    val postContent: String,
    val postImageUrls: List<String>?,
    val postThumbnailUrl: String?,
    // user 관련
    val userId: UUID,
    val userName: String,
    val userImageUrl: String?,
    val userThumbnailUrl: String?,
    // like 관련
    val numOfLikes: Long,
    val hasLike: Boolean
)

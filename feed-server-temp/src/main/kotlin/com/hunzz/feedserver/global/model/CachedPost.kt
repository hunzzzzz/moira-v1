package com.hunzz.feedserver.global.model

data class CachedPost(
    val postId: Long,
    val scope: String,
    val status: String,
    val content: String,
    val imageUrl: String?,
    val thumbnailUrl: String?
)

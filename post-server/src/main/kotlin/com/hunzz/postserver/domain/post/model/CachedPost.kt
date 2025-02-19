package com.hunzz.postserver.domain.post.model

data class CachedPost(
    val postId: Long,
    val scope: PostScope,
    val status: PostStatus,
    val content: String,
)
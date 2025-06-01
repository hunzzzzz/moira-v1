package com.hunzz.common.model.cache

import com.hunzz.common.model.property.PostScope
import com.hunzz.common.model.property.PostStatus
import java.util.*

data class PostInfo(
    val postId: UUID,
    val scope: PostScope,
    val status: PostStatus,
    val content: String,
    val imageUrls: List<String>,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), PostScope.PUBLIC, PostStatus.NORMAL, "", emptyList(), null)
}
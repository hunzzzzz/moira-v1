package com.hunzz.moirav1.domain.feed.dto.response

import com.hunzz.moirav1.domain.post.model.PostScope
import java.util.*

data class FeedResponse(
    val postId: Long,
    val userId: UUID,
    val userName: String,
    val userImageUrl: String?,
    val scope: PostScope,
    val content: String
)

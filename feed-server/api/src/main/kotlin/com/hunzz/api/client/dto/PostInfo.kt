package com.hunzz.api.client.dto

import java.util.*

data class PostInfo(
    val postId: UUID,
    val scope: String,
    val status: String,
    val content: String,
    val imageUrl: String?,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), "", "", "", null, null)
}

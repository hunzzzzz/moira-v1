package com.hunzz.postserver.domain.post.dto.response

import java.util.*

data class ImageInfo(
    val imageId: UUID,
    val imageUrl: String,
    val thumbnailUrl: String
)

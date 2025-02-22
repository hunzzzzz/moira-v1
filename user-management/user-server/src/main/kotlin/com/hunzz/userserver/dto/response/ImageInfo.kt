package com.hunzz.userserver.dto.response

import java.util.*

data class ImageInfo(
    val imageId: UUID,
    val imageUrl: String,
    val thumbnailUrl: String
)

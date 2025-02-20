package com.hunzz.imageserver.dto

import java.util.*

data class ImageResponse(
    val imageId: UUID,
    val imageUrl: String,
    val thumbnailUrl: String
)
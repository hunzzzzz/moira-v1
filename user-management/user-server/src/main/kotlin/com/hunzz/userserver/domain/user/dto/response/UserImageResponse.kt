package com.hunzz.userserver.domain.user.dto.response

import java.util.*

data class UserImageResponse(
    val imageId: UUID,
    val imageUrl: String,
    val thumbnailUrl: String
)

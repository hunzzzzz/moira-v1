package com.hunzz.api.domain.dto.client

import java.util.*

data class UserInfo(
    val userId: UUID,
    val name: String,
    val thumbnailUrl: String?
)

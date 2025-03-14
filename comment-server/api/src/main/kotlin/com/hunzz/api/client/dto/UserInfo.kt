package com.hunzz.api.client.dto

import java.util.*

data class UserInfo(
    val userId: UUID,
    val name: String,
    val thumbnailUrl: String?
)
package com.hunzz.common.domain.user.model

import java.util.*

data class CachedUser(
    val userId: UUID,
    val status: UserStatus,
    val name: String,
    val imageUrl: String?
)
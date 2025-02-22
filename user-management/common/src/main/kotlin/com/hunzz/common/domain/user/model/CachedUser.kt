package com.hunzz.common.domain.user.model

import java.util.*

data class CachedUser(
    val userId: UUID,
    val status: UserStatus,
    val name: String,
    val imageUrl: String?,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), UserStatus.NORMAL, "", null, null)
}
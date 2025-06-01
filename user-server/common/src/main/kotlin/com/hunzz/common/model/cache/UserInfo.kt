package com.hunzz.common.model.cache

import com.hunzz.common.model.property.UserStatus
import java.util.*

data class UserInfo(
    val userId: UUID,
    val status: UserStatus,
    val name: String,
    val imageUrl: String?,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), UserStatus.NORMAL, "", null, null)
}
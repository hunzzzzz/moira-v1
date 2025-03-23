package com.hunzz.api.client.dto

import java.util.*

data class UserInfo(
    val userId: UUID,
    val name: String,
    val imageUrl: String?,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), "", null, null)
}
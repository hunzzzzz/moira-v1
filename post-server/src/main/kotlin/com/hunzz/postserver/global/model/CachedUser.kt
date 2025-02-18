package com.hunzz.postserver.global.model

import java.util.*

data class CachedUser(
    val userId: UUID,
    val status: String,
    val name: String,
    val imageUrl: String?
)
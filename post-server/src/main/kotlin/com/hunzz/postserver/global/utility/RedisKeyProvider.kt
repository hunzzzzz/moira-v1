package com.hunzz.postserver.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // post
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
}
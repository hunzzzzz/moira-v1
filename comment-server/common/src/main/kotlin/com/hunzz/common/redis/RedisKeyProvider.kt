package com.hunzz.common.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // user
    fun user(userId: UUID) = "user:$userId"

    // post
    fun postAuthor(postId: UUID) = "post-author:$postId"
}
package com.hunzz.common.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // relation
    fun followers(userId: UUID) = "followers:${userId}"

    // feed-queue
    fun feedQueue() = "feed-queue"
    fun feedDeleteQueue() = "feed-delete-queue"
}
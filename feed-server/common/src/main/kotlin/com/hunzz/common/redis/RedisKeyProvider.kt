package com.hunzz.common.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // post
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"

    // feed-queue
    fun feedQueue() = "feed-queue"
    fun feedDeleteQueue() = "feed-delete-queue"
    fun feedReadQueue() = "feed-read-queue"
}
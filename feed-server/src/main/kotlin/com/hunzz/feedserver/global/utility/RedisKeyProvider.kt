package com.hunzz.feedserver.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"

    // post
    fun latestPosts(userId: UUID) = "latest-post:$userId"
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
    fun readFeedQueue() = "read-feed-queue"
}
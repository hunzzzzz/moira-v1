package com.hunzz.relationserver.utility.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // user
    fun emails() = "emails"
    fun ids() = "ids"
    fun user(userId: UUID) = "user:$userId"

    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"
    fun followQueue() = "follow_queue"
    fun unfollowQueue() = "unfollow_queue"
}
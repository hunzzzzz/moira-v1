package com.hunzz.feedserver.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"
}
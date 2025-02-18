package com.hunzz.postserver.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // user
    fun emails() = "emails"
    fun ids() = "ids"
    fun user(userId: UUID) = "user:$userId"

    // post
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
}
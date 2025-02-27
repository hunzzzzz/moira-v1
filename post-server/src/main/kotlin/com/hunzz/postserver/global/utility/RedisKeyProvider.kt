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
    fun post(postId: Long) = "post:$postId"
    fun latestPosts(userId: UUID) = "latest-post:$userId"
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
    fun likeNotification(postId: Long) = "post:$postId:like-notification"
}
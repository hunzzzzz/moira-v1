package com.hunzz.common.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // user
    fun user(userId: UUID) = "user:$userId"

    // post
    fun post(postId: UUID) = "post:$postId"
    fun postAuthor(postId: UUID) = "post-author:$postId"
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
    fun likeNotification(postId: UUID) = "post:$postId:like-notification"

    // post-transaction
    fun pending(txId: UUID) = "pending:$txId"
    fun rollback(txId: UUID) = "rollback:$txId"
}
package com.hunzz.api.component

import com.hunzz.api.dto.request.CommentRequest
import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.kafka.dto.KafkaAddCommentRequest
import com.hunzz.common.kafka.dto.KafkaDeleteCommentRequest
import com.hunzz.common.kafka.dto.KafkaPostCacheRequest
import com.hunzz.common.kafka.dto.KafkaUserCacheRequest
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class CommentKafkaHandler(
    private val kafkaProducer: KafkaProducer,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun addComment(userId: UUID, postId: UUID, commentId: UUID, request: CommentRequest) {
        val data = KafkaAddCommentRequest(
            userId = userId,
            postId = postId,
            commentId = commentId,
            content = request.content!!
        )

        kafkaProducer.send("add-comment", data)
    }

    fun deleteComment(userId: UUID, postId: UUID, commentId: UUID) {
        val data = KafkaDeleteCommentRequest(
            userId = userId,
            postId = postId,
            commentId = commentId
        )

        kafkaProducer.send("delete-comment", data)
    }

    fun addUserCache(userId: UUID) {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        if (redisTemplate.opsForValue().get(userCacheKey) == null) {
            val data = KafkaUserCacheRequest(userId = userId)

            kafkaProducer.send("add-user-cache", data)
        }
    }

    fun addPostCache(postId: UUID) {
        val data = KafkaPostCacheRequest(postId = postId)

        kafkaProducer.send("add-post-cache", data)
    }
}
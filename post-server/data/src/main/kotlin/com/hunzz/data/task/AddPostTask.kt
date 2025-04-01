package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.kafka.dto.KafkaAddPostRequest
import com.hunzz.common.model.Post
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.PostRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
class AddPostTask(
    private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["add-post"], groupId = "add-post")
    @Transactional
    fun addPost(message: String) {
        val data = objectMapper.readValue(message, KafkaAddPostRequest::class.java)

        // DB에 저장
        postRepository.save(
            Post(
                id = data.postId,
                txId = data.txId,
                scope = data.scope,
                content = data.content,
                userId = data.userId,
                imageUrls = data.imageUrls,
                thumbnailUrl = data.thumbnailUrl
            )
        )

        // 작업 완료 사실을 알림
        val pendingKey = redisKeyProvider.pending(txId = data.txId)
        redisTemplate.opsForSet().add(pendingKey, "data")
        redisTemplate.expire(pendingKey, 1, TimeUnit.HOURS)
    }
}
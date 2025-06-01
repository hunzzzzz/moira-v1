package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.kafka.dto.KafkaRollbackPostRequest
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.PostRepository
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RollbackPostTask(
    private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["rollback-post"], groupId = "rollback-post")
    @Transactional
    fun rollbackPost(message: String) {
        val data = objectMapper.readValue(message, KafkaRollbackPostRequest::class.java)

        postRepository.deleteByTxId(txId = data.txId)

        // 롤백 완료 사실을 알림
        val rollbackKey = redisKeyProvider.rollback(txId = data.txId)
        redisTemplate.opsForSet().add(rollbackKey, "data")
        redisTemplate.expire(rollbackKey, 1, TimeUnit.HOURS)
    }
}
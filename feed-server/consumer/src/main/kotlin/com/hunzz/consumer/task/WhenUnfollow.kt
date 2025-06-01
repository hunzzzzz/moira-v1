package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.consumer.dto.KafkaUpdateFeedWhenFollowRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WhenUnfollow(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    // 유저 A가 유저 B를 언팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 삭제된다.
    @KafkaListener(topics = ["delete-feed-when-unfollow"], groupId = "delete-feed-when-unfollow")
    fun whenUnfollow(message: String) {
        val data = objectMapper.readValue(message, KafkaUpdateFeedWhenFollowRequest::class.java)

        val script = """
            -- 세팅
            local user_id = ARGV[1]
            local author_id = ARGV[2]
            
            local feed_delete_queue_key = KEYS[1]
            
            -- 피드 삭제 큐에 데이터 저장
            local data = cjson.encode({
                userId = user_id,
                authorId = author_id,
            })
            redis.call('SADD', feed_delete_queue_key, data)
            
            return 'OK'
        """.trimIndent()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(redisKeyProvider.feedDeleteQueue()),
            data.userId.toString(),
            data.authorId.toString()
        )
    }
}
package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.consumer.client.PostServerClient
import com.hunzz.consumer.dto.KafkaUpdateFeedWhenFollowRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WhenFollow(
    private val objectMapper: ObjectMapper,
    private val postServerClient: PostServerClient,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    // 유저 A가 유저 B를 팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 추가된다.
    @KafkaListener(topics = ["update-feed-when-follow"], groupId = "update-feed-when-follow")
    fun whenFollow(message: String) {
        val data = objectMapper.readValue(message, KafkaUpdateFeedWhenFollowRequest::class.java)

        // 가장 최근 10개의 게시글을 가져온다.
        val postIds = postServerClient.getLatestPostIds(authorId = data.authorId)

        val script = """
            -- 세팅
            local latest_post_ids = cjson.decode(ARGV[1])
            local user_id = ARGV[2]
            local author_id = ARGV[3]
            
            local feed_queue_key = KEYS[1]
            
            -- 피드 큐에 데이터 저장
            for _, post_id in ipairs(latest_post_ids) do
                local data = cjson.encode({
                    userId = user_id,
                    postId = post_id,
                    authorId = author_id
                })
                redis.call('SADD', feed_queue_key, data)
            end
            
            return 'OK'
        """.trimIndent()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(redisKeyProvider.feedQueue()),
            objectMapper.writeValueAsString(postIds),
            data.userId.toString(),
            data.authorId.toString()
        )
    }
}

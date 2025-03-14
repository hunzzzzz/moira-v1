package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.consumer.dto.KafkaUpdateFeedWhenAddPostRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WhenAddPost(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyProvider: RedisKeyProvider
) {
    // 유저 A(author)가 게시글을 등록할 때, 유저 A를 팔로우하는 다른 유저(user)들의 큐에 해당 게시글을 등록한다.
    @KafkaListener(topics = ["update-feed-when-add-post"], groupId = "update-feed-when-add-post")
    fun whenAddPost(message: String) {
        val data = objectMapper.readValue(message, KafkaUpdateFeedWhenAddPostRequest::class.java)

        val script = """
            -- 세팅
            local author_id = ARGV[1]
            local post_id = ARGV[2]
            
            local followers_key = "followers:" .. author_id
            local feed_queue_key = KEYS[1]
            
            -- 게시글 작성자의 팔로워(id) 목록 조회
            local followers = redis.call('ZRANGE', followers_key, 0, -1)
            -- 게시글 작성자 본인의 피드에도 해당 게시글을 추가해야 한다.
            table.insert(followers, author_id)
            
            -- 피드 큐에 데이터 저장
            if followers then
                for _, follower_id in ipairs(followers) do
                    local data = cjson.encode({
                        userId = follower_id,
                        postId = post_id,
                        authorId = author_id
                    })
                    redis.call('SADD', feed_queue_key, data)
                end
            end
            
            return 'OK'
        """.trimIndent()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(redisKeyProvider.feedQueue()),
            data.authorId.toString(),
            data.postId.toString()
        )
    }
}
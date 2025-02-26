package com.hunzz.postserver.domain.post.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.postserver.domain.post.dto.request.KafkaPostRequest
import com.hunzz.postserver.domain.post.model.CachedPost
import com.hunzz.postserver.domain.post.model.PostLikeType
import com.hunzz.postserver.global.exception.ErrorCode.ALREADY_LIKED
import com.hunzz.postserver.global.exception.ErrorCode.ALREADY_UNLIKED
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import com.hunzz.postserver.global.utility.RedisKeyProvider
import com.hunzz.postserver.global.utility.RedisScriptProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class PostRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["add-post"], groupId = "post-server-add-post")
    fun addPost(message: String) {
        // settings
        val data = objectMapper.readValue(message, KafkaPostRequest::class.java)

        // settings
        val script = """
            local latest_post_key = KEYS[1]
            local post_id = ARGV[1]
            local current_time = ARGV[2]
            
            -- ZSET에 값 추가
            redis.call('ZADD', latest_post_key, current_time, post_id)
            
            -- ZSET의 크기 확인
            local size = redis.call('ZCARD', latest_post_key)
            if size > 10 then
                redis.call('ZREMRANGEBYRANK', latest_post_key, 0, 0)
            end
            
            return nil
        """.trimIndent()
        val latestPostsKey = redisKeyProvider.latestPosts(userId = data.authorId)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(latestPostsKey), // keys
            data.postId.toString(), // argv[1]
            System.currentTimeMillis().toString() // argv[2]
        )
    }

    fun getPostCache(postId: Long): CachedPost? {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        return redisTemplate.opsForValue().get(postCacheKey)
            ?.let { objectMapper.readValue(it, CachedPost::class.java) }
    }

    fun setPostCache(postId: Long, cachedPost: CachedPost) {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        redisTemplate.opsForValue().set(
            postCacheKey,
            objectMapper.writeValueAsString(cachedPost),
            6,
            TimeUnit.HOURS
        )
    }

    fun like(userId: UUID, postId: Long, type: PostLikeType) {
        // 세팅
        val script = when (type) {
            PostLikeType.LIKE -> redisScriptProvider.like()
            PostLikeType.UNLIKE -> redisScriptProvider.unlike()
        }
        val likeKey = redisKeyProvider.like(userId = userId)
        val likeCountKey = redisKeyProvider.likeCount()
        val likeNotificationKey = redisKeyProvider.likeNotification(postId = postId)
        val currentTime = System.currentTimeMillis()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(likeKey, likeCountKey, likeNotificationKey),
            postId.toString(),
            userId.toString(),
            currentTime.toString()
        )

        // 검증
        when (result) {
            ALREADY_LIKED.name -> throw InvalidPostInfoException(ALREADY_LIKED)
            ALREADY_UNLIKED.name -> throw InvalidPostInfoException(ALREADY_UNLIKED)
            else -> return
        }
    }
}
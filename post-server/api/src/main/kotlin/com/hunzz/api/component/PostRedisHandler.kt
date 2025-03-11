package com.hunzz.api.component

import com.hunzz.common.exception.ErrorCode.ALREADY_LIKED
import com.hunzz.common.exception.ErrorCode.ALREADY_UNLIKED
import com.hunzz.common.exception.custom.InvalidPostInfoException
import com.hunzz.common.model.property.PostLikeType
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class PostRedisHandler(
    private val postRedisScriptProvider: PostRedisScriptProvider,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun like(userId: UUID, postId: UUID, type: PostLikeType) {
        // 세팅
        val script = when (type) {
            PostLikeType.LIKE -> postRedisScriptProvider.like()
            PostLikeType.UNLIKE -> postRedisScriptProvider.unlike()
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
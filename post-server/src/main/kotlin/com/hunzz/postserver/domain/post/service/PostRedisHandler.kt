package com.hunzz.postserver.domain.post.service

import com.hunzz.postserver.domain.post.model.PostLikeType
import com.hunzz.postserver.global.exception.ErrorCode.ALREADY_LIKED
import com.hunzz.postserver.global.exception.ErrorCode.ALREADY_UNLIKED
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import com.hunzz.postserver.global.utility.RedisKeyProvider
import com.hunzz.postserver.global.utility.RedisScriptProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class PostRedisHandler(
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun like(userId: UUID, postId: Long, type: PostLikeType) {
        // settings
        val script = when (type) {
            PostLikeType.LIKE -> redisScriptProvider.like()
            PostLikeType.UNLIKE -> redisScriptProvider.unlike()
        }
        val likeKey = redisKeyProvider.like(userId = userId)
        val likeCountKey = redisKeyProvider.likeCount()
        val currentTime = System.currentTimeMillis()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(likeKey, likeCountKey), // keys
            postId.toString(), // argv[1]
            currentTime.toString() // argv[2]
        )

        // validate
        when (result) {
            ALREADY_LIKED.name -> throw InvalidPostInfoException(ALREADY_LIKED)
            ALREADY_UNLIKED.name -> throw InvalidPostInfoException(ALREADY_UNLIKED)
            else -> return
        }
    }
}
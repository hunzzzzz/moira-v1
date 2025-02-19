package com.hunzz.feedserver.domain.feed.service

import com.hunzz.feedserver.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class FeedRedisHandler(
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun getFollowers(authorId: UUID): MutableSet<String> {
        val followerKey = redisKeyProvider.follower(userId = authorId)

        return redisTemplate.opsForZSet().range(followerKey, 0, -1) ?: mutableSetOf()
    }

    fun getLatestPostIds(authorId: UUID): MutableSet<String> {
        val latestPostKey = redisKeyProvider.latestPosts(userId = authorId)

        return redisTemplate.opsForZSet().range(latestPostKey, 0, -1) ?: mutableSetOf()
    }
}
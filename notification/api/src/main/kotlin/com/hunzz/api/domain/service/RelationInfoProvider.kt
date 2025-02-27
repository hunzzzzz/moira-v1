package com.hunzz.api.domain.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RelationInfoProvider(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun isFollowing(actorId: String, targetId: String): Boolean {
        val followingKey = "following:$actorId"
        val isFollowing = redisTemplate.opsForZSet().score(followingKey, targetId) != null

        return isFollowing
    }
}
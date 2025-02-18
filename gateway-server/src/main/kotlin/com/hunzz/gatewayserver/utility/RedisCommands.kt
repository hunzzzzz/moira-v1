package com.hunzz.gatewayserver.utility

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisCommands(
    private val redisTemplate: RedisTemplate<String, String>
) {
    // string
    fun set(key: String, value: String): Unit =
        redisTemplate.opsForValue().set(key, value)

    fun get(key: String): String? =
        redisTemplate.opsForValue().get(key)
}
package com.hunzz.common.global.utility

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisCommands(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun delete(key: String): Boolean =
        redisTemplate.delete(key)

    fun deleteAll(): Unit =
        redisTemplate.keys("*").forEach { redisTemplate.delete(it) }

    // string
    fun set(key: String, value: String): Unit =
        redisTemplate.opsForValue().set(key, value)

    fun set(key: String, value: String, expirationTime: Long, timeUnit: TimeUnit): Unit =
        redisTemplate.opsForValue().set(key, value, expirationTime, timeUnit)

    fun get(key: String): String? =
        redisTemplate.opsForValue().get(key)

    // set
    fun sAdd(key: String, value: String): Long =
        redisTemplate.opsForSet().add(key, value)!!

    fun sIsMember(key: String, value: String): Boolean =
        redisTemplate.opsForSet().isMember(key, value)!!
}
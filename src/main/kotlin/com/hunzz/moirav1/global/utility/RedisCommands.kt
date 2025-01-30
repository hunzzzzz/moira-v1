package com.hunzz.moirav1.global.utility

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisCommands(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun delete(key: String): Boolean =
        redisTemplate.delete(key)

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

    // z-set
    fun zRange(key: String, start: Long, end: Long): MutableSet<String> =
        redisTemplate.opsForZSet().range(key, start, end)!!

    fun zAdd(key: String, value: String, score: Double): Boolean =
        redisTemplate.opsForZSet().add(key, value, score)!!

    fun zScore(key: String, value: String): Double? =
        redisTemplate.opsForZSet().score(key, value)

    fun zCard(key: String): Long =
        redisTemplate.opsForZSet().size(key)!!

    fun zRem(key: String, value: String): Long =
        redisTemplate.opsForZSet().remove(key, value)!!
}
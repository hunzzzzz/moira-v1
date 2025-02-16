package com.hunzz.userserver.global.client.utility

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.global.utility.RedisKeyProvider
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class RelationRedisCommandSender(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userHandler: UserHandler
) {
    @KafkaListener(topics = ["follow"], groupId = "user-server")
    fun follow(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val userId = UUID.fromString(data["userId"] as String)
        val targetId = UUID.fromString(data["targetId"] as String)

        // add user cache into redis
        listOf(userId, targetId).forEach {
            val followInfoKey = redisKeyProvider.user(userId = it)
            val followResponse = userHandler.getWithLocalCache(userId = it)

            redisTemplate.opsForValue().set(
                followInfoKey,
                objectMapper.writeValueAsString(followResponse),
                1,
                TimeUnit.DAYS
            )
        }
    }
}
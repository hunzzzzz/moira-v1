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
class RelationKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userHandler: UserHandler
) {
    @KafkaListener(topics = ["follow"], groupId = "user-server-follow")
    fun follow(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<List<UUID>>() {})

        // add user cache into redis
        data.forEach {
            val followInfoKey = redisKeyProvider.user(userId = it)
            val followResponse = userHandler.getWithLocalCache(userId = it)

            redisTemplate.opsForValue().set(
                followInfoKey,
                objectMapper.writeValueAsString(followResponse),
                3,
                TimeUnit.DAYS
            )
        }
    }
}
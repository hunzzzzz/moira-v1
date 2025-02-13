package com.hunzz.authserver.domain.auth.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AuthRedisCommandSender(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["login"], groupId = "auth-server")
    fun login(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val email = data["email"] as String
        val rtk = data["rtk"] as String

        // save rtk in redis
        val rtkKey = redisKeyProvider.rtk(email = email)
        redisTemplate.opsForValue().set(rtkKey, rtk)
    }
}
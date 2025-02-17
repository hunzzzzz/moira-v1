package com.hunzz.authserver.domain.auth.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AuthRedisCommandSender(
    private val authRedisHandler: AuthRedisHandler,
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["set-rtk"], groupId = "auth-server")
    fun setRtk(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val email = data["email"] as String
        val rtk = data["rtk"] as String

        val rtkKey = redisKeyProvider.rtk(email = email)
        redisTemplate.opsForValue().set(rtkKey, rtk)
    }

    @KafkaListener(topics = ["logout"], groupId = "auth-server")
    fun logout(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val email = data["email"] as String
        val atk = data["atk"] as String

        authRedisHandler.logout(atk = atk, email = email)
    }
}
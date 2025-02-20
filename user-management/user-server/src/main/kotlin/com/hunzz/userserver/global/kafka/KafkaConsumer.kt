package com.hunzz.userserver.global.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val userHandler: UserHandler
) {
    @KafkaListener(topics = ["add-user-cache"], groupId = "user-server-add-user-cache")
    fun addUserCache(message: String) {
        val userId = objectMapper.readValue(message, UUID::class.java)

        userHandler.getWithRedisCache(userId = userId)
    }

    @KafkaListener(topics = ["add-users-cache"], groupId = "user-server-add-users-cache")
    fun addUsersCache(message: String) {
        val userIds = objectMapper.readValue(message, object : TypeReference<List<UUID>>() {})

        // add user cache into redis
        userIds.forEach {
            userHandler.getWithRedisCache(userId = it)
        }
    }
}
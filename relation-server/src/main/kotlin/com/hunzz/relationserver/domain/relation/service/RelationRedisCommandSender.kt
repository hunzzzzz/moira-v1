package com.hunzz.relationserver.domain.relation.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class RelationRedisCommandSender(
    private val objectMapper: ObjectMapper,
    private val relationRedisScriptHandler: RelationRedisScriptHandler
) {
    @KafkaListener(topics = ["follow"], groupId = "relation-server")
    fun follow(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val userId = UUID.fromString(data["userId"] as String)
        val targetId = UUID.fromString(data["targetId"] as String)

        relationRedisScriptHandler.follow(userId = userId, targetId = targetId)
    }

    @KafkaListener(topics = ["unfollow"], groupId = "relation-server")
    fun unfollow(message: String) {
        val data = objectMapper.readValue(message, object : TypeReference<Map<String, Any>>() {})
        val userId = UUID.fromString(data["userId"] as String)
        val targetId = UUID.fromString(data["targetId"] as String)

        relationRedisScriptHandler.unfollow(userId = userId, targetId = targetId)
    }
}
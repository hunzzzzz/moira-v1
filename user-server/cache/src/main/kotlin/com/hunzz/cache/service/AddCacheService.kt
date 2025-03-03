package com.hunzz.cache.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.kafka.dto.KafkaAddUserCacheRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class AddCacheService(
    private val objectMapper: ObjectMapper,
    private val userCacheManager: UserCacheManager
) {
    @KafkaListener(topics = ["add-user-cache"], groupId = "add-user-cache")
    fun addCache(message: String) {
        val data = objectMapper.readValue(message, KafkaAddUserCacheRequest::class.java)

        userCacheManager.getWithLocalCache(userId = data.userId)
    }

    @KafkaListener(topics = ["re-add-user-cache"], groupId = "re-add-user-cache")
    fun reAddCache(message: String) {
        val data = objectMapper.readValue(message, KafkaAddUserCacheRequest::class.java)

        userCacheManager.evictLocalCache(userId = data.userId)
        userCacheManager.getWithLocalCache(userId = data.userId)
    }
}
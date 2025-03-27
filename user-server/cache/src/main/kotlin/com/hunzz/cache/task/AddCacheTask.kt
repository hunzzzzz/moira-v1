package com.hunzz.cache.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.kafka.dto.KafkaAddUserCacheRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class AddCacheTask(
    private val objectMapper: ObjectMapper,
    private val userCacheManager: UserCacheManager
) {
    @KafkaListener(topics = ["add-user-cache"], groupId = "add-user-cache")
    fun addCache(message: String) {
        val data = objectMapper.readValue(message, KafkaAddUserCacheRequest::class.java)

        userCacheManager.getWithLocalCache(userId = data.userId)
    }
}
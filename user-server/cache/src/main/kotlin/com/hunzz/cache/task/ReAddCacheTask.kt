package com.hunzz.cache.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.kafka.dto.KafkaAddUserCacheRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ReAddCacheTask(
    private val objectMapper: ObjectMapper,
    private val userCacheManager: UserCacheManager
) {
    @KafkaListener(topics = ["re-add-user-cache"], groupId = "re-add-user-cache")
    fun reAddCache(message: String) {
        val data = objectMapper.readValue(message, KafkaAddUserCacheRequest::class.java)

        userCacheManager.evictCaches(userId = data.userId)
        userCacheManager.getWithLocalCache(userId = data.userId)
    }
}
package com.hunzz.cache.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.cache.PostCacheManager
import com.hunzz.common.kafka.dto.KafkaPostCacheRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PostCacheTask(
    private val objectMapper: ObjectMapper,
    private val postCacheManager: PostCacheManager
) {
    @KafkaListener(topics = ["add-post-cache"], groupId = "add-post-cache")
    fun addPostCache(message: String) {
        val data = objectMapper.readValue(message, KafkaPostCacheRequest::class.java)

        postCacheManager.getWithLocalCache(postId = data.postId)
    }

    @KafkaListener(topics = ["re-add-post-cache"], groupId = "re-add-post-cache")
    fun reAddPostCache(message: String) {
        val data = objectMapper.readValue(message, KafkaPostCacheRequest::class.java)

        postCacheManager.evictLocalCache(postId = data.postId)
        postCacheManager.getWithLocalCache(postId = data.postId)
    }

    @KafkaListener(topics = ["delete-post-cache"], groupId = "delete-post-cache")
    fun deletePostCache(message: String) {
        val data = objectMapper.readValue(message, KafkaPostCacheRequest::class.java)

        postCacheManager.evictLocalCache(postId = data.postId)
    }
}
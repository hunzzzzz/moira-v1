package com.hunzz.postserver.global.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.postserver.domain.post.service.PostHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val postHandler: PostHandler
) {
    @KafkaListener(topics = ["add-post-cache"], groupId = "post-server-add-post-cache")
    fun addUserCache(message: String) {
        val postId = objectMapper.readValue(message, Long::class.java)

        postHandler.getCachedPostWithLocalCache(postId = postId)
    }
}
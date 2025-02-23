package com.hunzz.postserver.global.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.postserver.domain.comment.dto.request.KafkaCommentRequest
import com.hunzz.postserver.domain.post.service.PostHandler
import com.hunzz.postserver.global.utility.KafkaProducer
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
    private val kafkaProducer: KafkaProducer,
    private val objectMapper: ObjectMapper,
    private val postHandler: PostHandler
) {
    @KafkaListener(topics = ["add-post-cache"], groupId = "post-server-add-post-cache")
    fun addUserCache(message: String) {
        val postId = objectMapper.readValue(message, Long::class.java)

        postHandler.getCachedPostWithLocalCache(postId = postId)
    }

    @KafkaListener(topics = ["fill-post-author-id"], groupId = "post-server-fill-post-author-id")
    fun fillPostAuthorId(message: String) {
        val data = objectMapper.readValue(message, KafkaCommentRequest::class.java)

        data.postAuthorId = postHandler.getAuthorIdFromPostId(postId = data.postId)

        kafkaProducer.send("add-comment", data)
    }
}
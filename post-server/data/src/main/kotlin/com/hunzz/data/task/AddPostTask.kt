package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.kafka.dto.KafkaAddPostRequest
import com.hunzz.common.model.Post
import com.hunzz.common.repository.PostRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AddPostTask(
    private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository
) {
    @KafkaListener(topics = ["add-post"], groupId = "add-post")
    @Transactional
    fun addPost(message: String) {
        val data = objectMapper.readValue(message, KafkaAddPostRequest::class.java)

        postRepository.save(
            Post(
                id = data.postId,
                scope = data.scope,
                content = data.content,
                userId = data.userId,
                imageUrls = data.imageUrls,
                thumbnailUrl = data.thumbnailUrl
            )
        )
    }
}
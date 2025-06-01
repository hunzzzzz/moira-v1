package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.kafka.dto.KafkaAddCommentRequest
import com.hunzz.common.model.Comment
import com.hunzz.common.repository.CommentRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AddCommentTask(
    private val objectMapper: ObjectMapper,
    private val commentRepository: CommentRepository
) {
    @KafkaListener(topics = ["add-comment"], groupId = "add-comment")
    @Transactional
    fun addComment(message: String) {
        val data = objectMapper.readValue(message, KafkaAddCommentRequest::class.java)

        commentRepository.save(
            Comment(
                id = data.commentId,
                content = data.content,
                userId = data.userId,
                postId = data.postId
            )
        )
    }
}
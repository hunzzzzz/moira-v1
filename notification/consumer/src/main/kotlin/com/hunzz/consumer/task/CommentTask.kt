package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.model.CommentNotification
import com.hunzz.common.repository.NotificationRepository
import com.hunzz.consumer.dto.KafkaCommentRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CommentTask(
    private val objectMapper: ObjectMapper,
    private val notificationRepository: NotificationRepository
) {
    @KafkaListener(topics = ["add-comment"], groupId = "notification-server-add-comment")
    fun comment(message: String) {
        val data = objectMapper.readValue(message, KafkaCommentRequest::class.java)

        if (data.postAuthorId != data.commentAuthorId)
            notificationRepository.save(
                CommentNotification(
                    postAuthorId = data.postAuthorId,
                    commentAuthorId = data.commentAuthorId,
                    postId = data.postId,
                    commentId = data.commentId,
                )
            )
    }
}
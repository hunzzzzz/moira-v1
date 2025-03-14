package com.hunzz.api.service

import com.hunzz.api.component.CommentKafkaHandler
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeleteCommentService(
    private val commentKafkaHandler: CommentKafkaHandler
) {
    fun deleteComment(userId: UUID, postId: UUID, commentId: UUID) {
        commentKafkaHandler.deleteComment(userId = userId, postId = postId, commentId = commentId)
    }
}
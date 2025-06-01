package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.exception.ErrorCode.*
import com.hunzz.common.exception.custom.InvalidCommentInfoException
import com.hunzz.common.kafka.dto.KafkaDeleteCommentRequest
import com.hunzz.common.model.Comment
import com.hunzz.common.repository.CommentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UpdateCommentTask(
    private val commentRepository: CommentRepository,
    private val objectMapper: ObjectMapper
) {
    private fun isAuthorOfComment(userId: UUID, comment: Comment) {
        if (comment.userId != userId)
            throw InvalidCommentInfoException(CANNOT_UPDATE_OTHERS_COMMENT)
    }

    private fun isBelongToPost(postId: UUID, comment: Comment) {
        if (comment.postId != postId)
            throw InvalidCommentInfoException(COMMENT_NOT_BELONGS_TO_POST)
    }

    @KafkaListener(topics = ["delete-comment"], groupId = "delete-comment")
    @Transactional
    fun deleteComment(message: String) {
        val data = objectMapper.readValue(message, KafkaDeleteCommentRequest::class.java)

        // Comment 객체 조회
        val comment = commentRepository.findByIdOrNull(id = data.commentId)
            ?: throw InvalidCommentInfoException(COMMENT_NOT_FOUND)

        // 검증
        isAuthorOfComment(userId = data.userId, comment = comment)
        isBelongToPost(postId = data.postId, comment = comment)

        // 삭제
        comment.delete()
    }
}
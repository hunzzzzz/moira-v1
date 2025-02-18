package com.hunzz.postserver.domain.comment.service

import com.hunzz.postserver.domain.comment.dto.request.CommentRequest
import com.hunzz.postserver.domain.comment.model.Comment
import com.hunzz.postserver.domain.comment.repository.CommentRepository
import com.hunzz.postserver.global.exception.ErrorCode
import com.hunzz.postserver.global.exception.ErrorCode.CANNOT_UPDATE_OTHERS_COMMENT
import com.hunzz.postserver.global.exception.ErrorCode.COMMENT_NOT_BELONGS_TO_POST
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class CommentHandler(
    private val commentRepository: CommentRepository
) {
    private fun validateComment(userId: UUID, postId: Long, comment: Comment) {
        if (userId != comment.userId)
            throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_COMMENT)
        if (postId != comment.postId)
            throw InvalidPostInfoException(COMMENT_NOT_BELONGS_TO_POST)
    }

    @Transactional
    fun add(userId: UUID, postId: Long, request: CommentRequest): Long {
        commentRepository.save(
            Comment(
                content = request.content!!,
                userId = userId,
                postId = postId
            )
        )

        return postId
    }

    fun get(commentId: Long): Comment {
        val comment = commentRepository.findByIdOrNull(id = commentId)
            ?: throw InvalidPostInfoException(ErrorCode.COMMENT_NOT_FOUND)

        return comment
    }

    @Transactional
    fun update(userId: UUID, postId: Long, commentId: Long, request: CommentRequest) {
        // get comment
        val comment = get(commentId = commentId)

        // validate
        validateComment(userId = userId, postId = postId, comment = comment)

        // update
        comment.update(request = request)
    }

    @Transactional
    fun delete(userId: UUID, postId: Long, commentId: Long) {
        // get comment
        val comment = get(commentId = commentId)

        // validate
        validateComment(userId = userId, postId = postId, comment = comment)

        // soft delete
        comment.delete()
    }
}
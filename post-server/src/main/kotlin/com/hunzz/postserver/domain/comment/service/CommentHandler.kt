package com.hunzz.postserver.domain.comment.service

import com.hunzz.postserver.domain.comment.dto.request.CommentRequest
import com.hunzz.postserver.domain.comment.dto.request.KafkaCommentRequest
import com.hunzz.postserver.domain.comment.dto.response.CommentResponse
import com.hunzz.postserver.domain.comment.dto.response.CommentSliceResponse
import com.hunzz.postserver.domain.comment.model.Comment
import com.hunzz.postserver.domain.comment.repository.CommentRepository
import com.hunzz.postserver.global.aop.cache.PostCache
import com.hunzz.postserver.global.aop.cache.UserCache
import com.hunzz.postserver.global.exception.ErrorCode
import com.hunzz.postserver.global.exception.ErrorCode.CANNOT_UPDATE_OTHERS_COMMENT
import com.hunzz.postserver.global.exception.ErrorCode.COMMENT_NOT_BELONGS_TO_POST
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import com.hunzz.postserver.global.utility.KafkaProducer
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class CommentHandler(
    private val commentRedisHandler: CommentRedisHandler,
    private val commentRepository: CommentRepository,
    private val kafkaProducer: KafkaProducer
) {
    @Transactional
    @UserCache
    @PostCache
    fun add(userId: UUID, postId: Long, request: CommentRequest): Long {
        // save
        val comment = commentRepository.save(
            Comment(
                content = request.content!!,
                userId = userId,
                postId = postId
            )
        )

        // send kafka message (to post-server)
        val kafkaCommentRequest = KafkaCommentRequest(
            postAuthorId = UUID.randomUUID(), // TODO
            commentAuthorId = userId,
            postId = postId,
            commentId = comment.id!!
        )
        kafkaProducer.send("fill-post-author-id", data = kafkaCommentRequest)

        return postId
    }

    // ------------------------------------------------------------------------- //

    fun getAll(postId: Long, cursor: Long?): CommentSliceResponse {
        val pageable = PageRequest.ofSize(10)

        // get comment infos from db
        val comments = commentRepository.getComments(
            pageable = pageable,
            postId = postId,
            cursor = cursor
        )
        // get user infos from redis & user-server
        val userInfos = commentRedisHandler.getUserInfo(
            userIds = comments.map { it.userId.toString() }
        )

        // combine
        val contents = comments.mapIndexed { index, comment ->
            val userInfo = userInfos[index]

            CommentResponse(
                commentId = comment.commentId,
                status = comment.status,
                content = comment.content,
                userId = comment.userId,
                userName = userInfo.name,
                userImageUrl = userInfo.imageUrl,
                userThumbnailUrl = userInfo.thumbnailUrl
            )
        }

        return CommentSliceResponse(
            currentCursor = cursor,
            nextCursor = contents.lastOrNull()?.commentId,
            contents = contents
        )
    }

    // ------------------------------------------------------------------------- //
    private fun get(commentId: Long): Comment {
        val comment = commentRepository.findByIdOrNull(id = commentId)
            ?: throw InvalidPostInfoException(ErrorCode.COMMENT_NOT_FOUND)

        return comment
    }

    private fun validateComment(userId: UUID, postId: Long, comment: Comment) {
        if (userId != comment.userId)
            throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_COMMENT)
        if (postId != comment.postId)
            throw InvalidPostInfoException(COMMENT_NOT_BELONGS_TO_POST)
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
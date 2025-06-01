package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.exception.ErrorCode.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.common.exception.ErrorCode.POST_NOT_FOUND
import com.hunzz.common.exception.custom.InvalidPostInfoException
import com.hunzz.common.kafka.dto.KafkaDeletePostRequest
import com.hunzz.common.kafka.dto.KafkaUpdatePostRequest
import com.hunzz.common.model.Post
import com.hunzz.common.repository.PostRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UpdatePostTask(
    private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository
) {
    private fun getPost(postId: UUID): Post {
        val post = postRepository.findByIdOrNull(id = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return post
    }

    private fun isAuthorOfPost(userId: UUID, post: Post) {
        if (userId != post.userId)
            throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_POST)
    }

    @KafkaListener(topics = ["update-post"], groupId = "update-post")
    @Transactional
    fun updatePost(message: String) {
        val data = objectMapper.readValue(message, KafkaUpdatePostRequest::class.java)

        // Post 객체 가져오기
        val post = getPost(postId = data.postId)

        // 검증
        isAuthorOfPost(userId = data.userId, post = post)

        // 업데이트
        post.update(content = data.content, scope = data.scope)
    }

    @KafkaListener(topics = ["delete-post"], groupId = "delete-post")
    @Transactional
    fun softDeletePost(message: String) {
        val data = objectMapper.readValue(message, KafkaDeletePostRequest::class.java)

        // Post 객체 가져오기
        val post = getPost(postId = data.postId)

        // 검증
        isAuthorOfPost(userId = data.userId, post = post)

        // soft-delete
        post.delete()
    }
}
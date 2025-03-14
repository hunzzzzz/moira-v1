package com.hunzz.api.service

import com.hunzz.api.component.CommentKafkaHandler
import com.hunzz.api.dto.request.CommentRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import java.util.*

@Service
class AddCommentService(
    private val commentKafkaHandler: CommentKafkaHandler
) {
    suspend fun addComment(userId: UUID, postId: UUID, request: CommentRequest): UUID = coroutineScope {
        val commentId = UUID.randomUUID()

        withTimeout(5_000) {
            // 작업1: DB에 댓글 등록
            val job1 = async {
                // Kafka 메시지 전송 (comment-api -> comment-data)
                commentKafkaHandler.addComment(
                    userId = userId,
                    postId = postId,
                    commentId = commentId,
                    request = request
                )
            }

            // TODO
            // 작업2: 게시글 작성자에게 알림 전송
            val job2 = async { }

            // 작업3: 게시글 캐시 등록
            val job3 = async { commentKafkaHandler.addPostCache(postId = postId) }

            // 작업4: 유저 캐시 등록
            val job4 = async { commentKafkaHandler.addUserCache(userId = userId) }

            awaitAll(job1, job2, job3, job4)
        }

        return@coroutineScope commentId
    }
}
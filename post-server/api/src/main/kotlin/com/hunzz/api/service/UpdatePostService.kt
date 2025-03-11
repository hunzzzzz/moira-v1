package com.hunzz.api.service

import com.hunzz.api.component.PostKafkaHandler
import com.hunzz.api.dto.request.PostRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import java.util.*

@Service
class UpdatePostService(
    private val postKafkaHandler: PostKafkaHandler
) {
    suspend fun update(userId: UUID, postId: UUID, request: PostRequest): Unit = coroutineScope {
        withTimeout(5_000) {
            // 작업1: 게시글 수정 요청
            val job1 = async {
                // Kafka 메시지 전송 (post-api -> post-data)
                postKafkaHandler.updatePost(postId = postId, userId = userId, request = request)
            }
            // 작업2: 게시글 캐시 재등록
            val job2 = async {
                postKafkaHandler.reAddPostCache(postId = postId)
            }
            awaitAll(job1, job2)
        }
    }

    suspend fun delete(userId: UUID, postId: UUID): Unit = coroutineScope {
        withTimeout(5_000) {
            // 작업1: 게시글 삭제 요청 (soft-delete)
            val job1 = async {
                // Kafka 메시지 전송 (post-api -> post-data)
                postKafkaHandler.deletePost(postId = postId, userId = userId)
            }
            // 작업2: 게시글 캐시 삭제 요청
            val job2 = async {
                // Kafka 메시지 전송 (post-api -> post-cache)
                postKafkaHandler.deletePostCache(postId = postId)
            }
            awaitAll(job1, job2)
        }
    }
}
package com.hunzz.api.service

import com.hunzz.api.component.PostImageHandler
import com.hunzz.api.component.PostKafkaHandler
import com.hunzz.api.dto.request.PostRequest
import com.hunzz.common.model.property.PostScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AddPostService(
    private val postImageHandler: PostImageHandler,
    private val postKafkaHandler: PostKafkaHandler
) {
    suspend fun addPost(userId: UUID, request: PostRequest, image: MultipartFile?): UUID =
        coroutineScope {
            // 이미지 업로드 요청 (to image-server)
            val (originalFileUrl, thumbnailFileUrl) = postImageHandler.sendImageUploadRequest(image = image)

            // postId 생성
            val postId = UUID.randomUUID()

            withTimeout(5_000) {
                // 작업1: DB에 게시글 등록
                val job1 = async {
                    // Kafka 메시지 전송 (post-api -> post-data)
                    postKafkaHandler.savePost(
                        postId = postId,
                        userId = userId,
                        request = request,
                        imageUrl = originalFileUrl,
                        thumbnailUrl = thumbnailFileUrl
                    )
                }

                // 작업2: 나를 팔로우하는 유저들 피드에 게시글 추가
                val job2 = async {
                    // Kafka 메시지 전송 (post-api -> feed-server)
                    if (PostScope.valueOf(request.scope!!) != PostScope.PRIVATE)
                        postKafkaHandler.updateFeed(authorId = userId, postId = postId)
                }

                // 작업3: 게시글 캐시 등록
                val job3 = async { postKafkaHandler.addPostCache(postId = postId) }

                // 작업4: 유저 캐시 등록
                val job4 = async { postKafkaHandler.addUserCache(userId = userId) }

                // 작업5: 게시글 작성자 id 캐시 등록
                val job5 = async { postKafkaHandler.addPostAuthorCache(postId = postId, authorId = userId) }

                awaitAll(job1, job2, job3, job4, job5)
            }

            return@coroutineScope postId
        }
}
package com.hunzz.api.service

import com.hunzz.api.cache.PostCache
import com.hunzz.api.cache.UserCache
import com.hunzz.api.component.PostKafkaHandler
import com.hunzz.api.component.validation.PostImageHandler
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
    private val postKafkaHandler: PostKafkaHandler,
    private val postImageHandler: PostImageHandler
) {
    @UserCache
    @PostCache
    suspend fun addPost(
        userId: UUID,
        request: PostRequest,
        images: List<MultipartFile>?
    ) = coroutineScope {
        // 이미지 개수 검증
        if (images != null) postImageHandler.validateImages(images = images)

        // 세팅
        val postId = UUID.randomUUID()
        val originalFileNames =
            images?.let { postImageHandler.generateOriginalFileNames(postId = postId, images = images) }
        val thumbnailFileName = images?.let { postImageHandler.generateThumbnailFileNames(postId = postId) }

        withTimeout(5_000) {
            // 작업1: AWS S3에 이미지 업로드
            val job1 = async {
                if (images != null)
                    postKafkaHandler.uploadPostImages(
                        originalFileNames = originalFileNames!!,
                        thumbnailFileName = thumbnailFileName!!,
                        images = images
                    )
            }

            // 작업2: DB에 저장
            val job2 = async {
                postKafkaHandler.savePost(
                    postId = postId,
                    userId = userId,
                    request = request,
                    originalFileNames = originalFileNames,
                    thumbnailFileName = thumbnailFileName
                )
            }

            // 작업3: 나를 팔로우하는 유저들 피드에 게시글 추가
            val job3 = async {
                if (PostScope.valueOf(request.scope!!) != PostScope.PRIVATE)
                    postKafkaHandler.updateFeed(authorId = userId, postId = postId)
            }

            awaitAll(job1, job2, job3)
        }

        return@coroutineScope
    }
}
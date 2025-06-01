package com.hunzz.api.service

import com.hunzz.api.cache.PostCache
import com.hunzz.api.cache.UserCache
import com.hunzz.api.component.PostImageHandler
import com.hunzz.api.component.PostKafkaHandler
import com.hunzz.api.dto.request.PostRequest
import com.hunzz.common.model.PostTransaction
import com.hunzz.common.model.PostTransactionStatus
import com.hunzz.common.model.property.PostScope
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.PostTransactionRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Service
class AddPostService(
    private val postKafkaHandler: PostKafkaHandler,
    private val postImageHandler: PostImageHandler,
    private val postTransactionRepository: PostTransactionRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @UserCache
    @PostCache
    suspend fun addPost(
        userId: UUID,
        request: PostRequest,
        images: List<MultipartFile>?
    ) = coroutineScope {
        // 1. 검증
        if (images != null) postImageHandler.validateImages(images = images)

        // 2. 세팅
        val postId = UUID.randomUUID()
        val originalFileNames =
            images?.let { postImageHandler.generateOriginalFileNames(postId = postId, images = images) }
        val thumbnailFileName = images?.let { postImageHandler.generateThumbnailFileNames(postId = postId) }
        val isFailed = AtomicBoolean(false)
        val txId = UUID.randomUUID()

        // 3. 트랜잭션 실행
        runCatching {
            withTimeout(5_000) {
                supervisorScope {
                    listOf(
                        // 작업1: AWS S3에 이미지 업로드
                        async {
                            if (images != null)
                                postKafkaHandler.uploadPostImages(
                                    txId = txId,
                                    userId = userId,
                                    originalFileNames = originalFileNames!!,
                                    thumbnailFileName = thumbnailFileName!!,
                                    images = images
                                )
                        },
                        // 작업2: DB에 저장
                        async {
                            postKafkaHandler.savePost(
                                txId = txId,
                                postId = postId,
                                userId = userId,
                                request = request,
                                originalFileNames = originalFileNames,
                                thumbnailFileName = thumbnailFileName
                            )
                        },
                        // 작업3: 나를 팔로우하는 유저들 피드에 게시글 추가
                        async {
                            if (PostScope.valueOf(request.scope!!) != PostScope.PRIVATE)
                                postKafkaHandler.updateFeed(txId = txId, authorId = userId, postId = postId)
                        }
                    ).awaitAll()
                }
            }
        }.onFailure { e ->
            rollback(txId = txId)
            isFailed.set(true)

            logger.error("[작업 전달 실패] Kafka 오류 발생")
            throw e
        }.also {
            val repeatTime = 5

            if (!isFailed.get()) {
                repeat(repeatTime) { index ->
                    val pendingKey = redisKeyProvider.pending(txId = txId)

                    if (redisTemplate.opsForSet().size(pendingKey) == 3L) {
                        redisTemplate.delete(pendingKey)
                        return@repeat
                    } else {
                        isFailed.set(true)

                        if (index == repeatTime - 1)
                            rollback(txId = txId)
                        else delay(1000)
                    }
                }
            }

            savePostTransaction(txId = txId, isFailed = isFailed.get())
        }

        return@coroutineScope
    }

    private fun rollback(txId: UUID) {
        // 이미지 삭제
        postKafkaHandler.deletePostImages(txId = txId)
        // 게시글 삭제
        postKafkaHandler.rollbackPost(txId = txId)
        // 피드 삭제
        postKafkaHandler.rollbackFeed(txId = txId)
    }

    private fun savePostTransaction(txId: UUID, isFailed: Boolean) {
        PostTransaction(
            txId = txId,
            status = if (isFailed) PostTransactionStatus.FAILED else PostTransactionStatus.COMPLETED
        ).let { postTransactionRepository.save(it) }
    }
}
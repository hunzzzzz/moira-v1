package com.hunzz.api.component

import com.hunzz.api.dto.request.PostRequest
import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.kafka.dto.*
import com.hunzz.common.model.property.PostScope
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class PostKafkaHandler(
    private val kafkaProducer: KafkaProducer,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun uploadPostImage(originalFileName: String, thumbnailFileName: String, image: MultipartFile) {
        val data = KafkaImageUploadRequest(
            originalFileName = originalFileName,
            thumbnailFileName = thumbnailFileName,
            image = image.bytes
        )

        kafkaProducer.send("upload-image", data)
    }

    fun savePost(postId: UUID, userId: UUID, request: PostRequest, imageUrl: String?, thumbnailUrl: String?) {
        val data = KafkaAddPostRequest(
            postId = postId,
            content = request.content!!,
            scope = PostScope.valueOf(request.scope!!),
            userId = userId,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl
        )

        kafkaProducer.send("add-post", data)
    }

    fun updatePost(postId: UUID, userId: UUID, request: PostRequest) {
        val data = KafkaUpdatePostRequest(
            postId = postId,
            userId = userId,
            content = request.content!!,
            scope = PostScope.valueOf(request.scope!!),
        )

        kafkaProducer.send("update-post", data)
    }

    fun deletePost(postId: UUID, userId: UUID) {
        val data = KafkaDeletePostRequest(postId = postId, userId = userId)

        kafkaProducer.send("delete-post", data)
    }

    fun updateFeed(authorId: UUID, postId: UUID) {
        val data = KafkaAddFeedRequest(
            authorId = authorId,
            postId = postId
        )

        kafkaProducer.send("update-feed-when-add-post", data)
    }

    fun addUserCache(userId: UUID) {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        if (redisTemplate.opsForValue().get(userCacheKey) == null) {
            val data = KafkaUserCacheRequest(userId = userId)

            kafkaProducer.send("add-user-cache", data)
        }
    }

    fun addPostCache(postId: UUID) {
        val data = KafkaPostCacheRequest(postId = postId)

        kafkaProducer.send("add-post-cache", data)
    }

    fun addPostAuthorCache(postId: UUID, authorId: UUID) {
        val data = KafkaPostAuthorCacheRequest(
            postId = postId,
            authorId = authorId
        )

        kafkaProducer.send("add-post-author-cache", data)
    }

    fun reAddPostCache(postId: UUID) {
        val data = KafkaPostCacheRequest(postId = postId)

        kafkaProducer.send("re-add-post-cache", data)
    }

    fun deletePostCache(postId: UUID) {
        val data = KafkaPostCacheRequest(postId = postId)

        kafkaProducer.send("delete-post-cache", data)
    }
}
package com.hunzz.postserver.domain.post.service

import com.hunzz.postserver.domain.post.dto.request.KafkaImageRequest
import com.hunzz.postserver.domain.post.dto.request.KafkaPostRequest
import com.hunzz.postserver.domain.post.dto.request.PostRequest
import com.hunzz.postserver.domain.post.model.CachedPost
import com.hunzz.postserver.domain.post.model.Post
import com.hunzz.postserver.domain.post.model.PostLikeType
import com.hunzz.postserver.domain.post.model.PostScope
import com.hunzz.postserver.domain.post.repository.PostRepository
import com.hunzz.postserver.global.aop.cache.PostCache
import com.hunzz.postserver.global.aop.cache.UserCache
import com.hunzz.postserver.global.exception.ErrorCode.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.postserver.global.exception.ErrorCode.POST_NOT_FOUND
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import com.hunzz.postserver.global.utility.KafkaProducer
import org.springframework.aop.framework.AopContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class PostHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val kafkaProducer: KafkaProducer,
    private val postRedisHandler: PostRedisHandler,
    private val postRepository: PostRepository
) {
    private fun proxy() = AopContext.currentProxy() as PostHandler

    fun get(postId: Long): Post {
        val post = postRepository.findByIdOrNull(id = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return post
    }

    fun getCachedPost(postId: Long): CachedPost {
        val post = get(postId = postId)

        return CachedPost(
            postId = post.id!!,
            scope = post.scope,
            status = post.status,
            content = post.content,
            imageUrl = post.imageUrl,
            thumbnailUrl = post.thumbnailUrl
        )
    }

    fun getCachedPostWithRedisCache(postId: Long): CachedPost {
        val postCache = postRedisHandler.getPostCache(postId = postId)

        return if (postCache == null) {
            val cachedPost = getCachedPost(postId = postId)
            postRedisHandler.setPostCache(postId = postId, cachedPost = cachedPost)

            cachedPost
        } else postCache
    }

    @Cacheable(cacheNames = ["post"], key = "#postId", cacheManager = "localCacheManager")
    fun getCachedPostWithLocalCache(postId: Long): CachedPost {
        return getCachedPostWithRedisCache(postId = postId)
    }

    fun getAll(postIds: List<Long>): HashMap<Long, CachedPost> {
        val hashMap = hashMapOf<Long, CachedPost>()

        postIds.forEach {
            hashMap[it] = proxy().getCachedPostWithLocalCache(postId = it)
        }

        return hashMap
    }

    private fun getImageFileNames(): Pair<String, String> {
        val imageId = UUID.randomUUID()

        val originalFileName = "${imageId}.jpg"
        val thumbnailFileName = "${imageId}-thumbnail.jpg"

        return Pair(originalFileName, thumbnailFileName)
    }

    private fun getImageUrl(fileName: String): String {
        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }

    @Transactional
    @UserCache
    fun save(userId: UUID, request: PostRequest, image: MultipartFile?): Long {
        // send kafka message (to image server / save images)
        val (originalFileName, thumbnailFileName) = getImageFileNames()

        if (image != null) {
            val data = KafkaImageRequest(
                originalFileName = originalFileName,
                thumbnailFileName = thumbnailFileName,
                image = image.bytes
            )
            kafkaProducer.send(topic = "add-image", data)
        }

        // save 'post' in db
        val post = Post(
            scope = request.scope.let { PostScope.valueOf(it!!) },
            content = request.content!!,
            userId = userId,
            imageUrl = if (image != null) getImageUrl(fileName = originalFileName) else null,
            thumbnailUrl = if (image != null) getImageUrl(fileName = thumbnailFileName) else null
        )
        val postId = postRepository.save(post).id!!

        // send kafka message (to feed-server / add post into feed)
        if (post.scope != PostScope.PRIVATE) {
            val data = KafkaPostRequest(authorId = userId, postId = postId)
            kafkaProducer.send(topic = "add-post", data)
        }

        // send kafka message (to post-server / add post cache)
        kafkaProducer.send(topic = "add-post-cache", data = postId)

        return postId
    }

    @PostCache
    fun like(userId: UUID, postId: Long) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.LIKE)
    }

    fun unlike(userId: UUID, postId: Long) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.UNLIKE)
    }

    private fun isAuthorOfPost(userId: UUID, post: Post) {
        if (userId != post.userId)
            throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_POST)
    }

    @Transactional
    fun update(userId: UUID, postId: Long, request: PostRequest) {
        // get
        val post = this.get(postId = postId)

        // validate
        isAuthorOfPost(userId = userId, post = post)

        // update
        post.update(request = request)
    }

    @Transactional
    fun delete(userId: UUID, postId: Long) {
        // get
        val post = this.get(postId = postId)

        // validate
        isAuthorOfPost(userId = userId, post = post)

        // soft-delete
        post.delete()
    }
}
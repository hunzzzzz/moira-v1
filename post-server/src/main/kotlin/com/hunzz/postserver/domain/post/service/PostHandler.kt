package com.hunzz.postserver.domain.post.service

import com.hunzz.postserver.domain.post.dto.request.AddPostKafkaRequest
import com.hunzz.postserver.domain.post.dto.request.PostRequest
import com.hunzz.postserver.domain.post.model.CachedPost
import com.hunzz.postserver.domain.post.model.Post
import com.hunzz.postserver.domain.post.model.PostLikeType
import com.hunzz.postserver.domain.post.model.PostScope
import com.hunzz.postserver.domain.post.repository.PostRepository
import com.hunzz.postserver.global.aop.cache.UserCache
import com.hunzz.postserver.global.exception.ErrorCode.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.postserver.global.exception.ErrorCode.POST_NOT_FOUND
import com.hunzz.postserver.global.exception.custom.InvalidPostInfoException
import com.hunzz.postserver.global.utility.KafkaProducer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PostHandler(
    private val kafkaProducer: KafkaProducer,
    private val postRedisHandler: PostRedisHandler,
    private val postRepository: PostRepository
) {
    private fun isAuthorOfPost(userId: UUID, post: Post) {
        if (userId != post.userId)
            throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_POST)
    }

    fun get(postId: Long): Post {
        val post = postRepository.findByIdOrNull(id = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return post
    }

    fun getCachedPost(postId: Long): CachedPost {
        val post = postRepository.findByIdOrNull(id = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return CachedPost(
            postId = post.id!!,
            scope = post.scope,
            status = post.status,
            content = post.content
        )
    }

    fun getAll(postIds: List<Long>): List<CachedPost> {
        return postIds.map {
            this.getCachedPost(postId = it)
        }
    }

    @Transactional
    @UserCache
    fun save(userId: UUID, request: PostRequest): Long {
        // save 'post' in db
        val post = Post(
            scope = request.scope.let { PostScope.valueOf(it!!) },
            content = request.content!!,
            userId = userId
        )
        val postId = postRepository.save(post).id!!

        // send kafka message (to feed-server)
        if (post.scope != PostScope.PRIVATE) {
            val data = AddPostKafkaRequest(authorId = userId, postId = postId)
            kafkaProducer.send(topic = "add-post", data)
        }

        return postId
    }

    fun like(userId: UUID, postId: Long) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.LIKE)
    }

    fun unlike(userId: UUID, postId: Long) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.UNLIKE)
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
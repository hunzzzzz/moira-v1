package com.hunzz.moirav1.domain.post.service

import com.hunzz.moirav1.domain.feed.service.FeedEventHandler
import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.Post
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.post.repository.PostRepository
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_LIKED
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_UNLIKED
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.moirav1.global.exception.ErrorMessages.POST_NOT_FOUND
import com.hunzz.moirav1.global.exception.custom.InvalidPostInfoException
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import org.springframework.aop.framework.AopContext
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PostHandler(
    private val feedEventHandler: FeedEventHandler,
    private val postRepository: PostRepository,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) {
    private fun proxy() = AopContext.currentProxy() as PostHandler

    private fun isAuthorOfPost(userId: UUID, post: Post) {
        val condition = userId == post.userId

        require(condition) { throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_POST) }
    }

    private fun isNotAlreadyLiked(likeKey: String, postId: Long, isUnlike: Boolean) {
        val condition = (redisCommands.zScore(key = likeKey, value = postId.toString()) == null)
            .let { if (isUnlike) !it else it }

        require(condition) {
            throw InvalidPostInfoException(
                message = if (isUnlike) ALREADY_UNLIKED else ALREADY_LIKED
            )
        }
    }

    fun get(postId: Long): Post {
        val post = postRepository.findByIdOrNull(id = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return post
    }

    @Cacheable(cacheNames = ["post"], cacheManager = "redisCacheManager")
    fun getWithRedisCache(postId: Long): Post {
        return get(postId = postId)
    }

    @Cacheable(cacheNames = ["post"], cacheManager = "localCacheManager")
    fun getWithLocalCache(postId: Long): Post {
        return getWithRedisCache(postId = postId)
    }

    @Transactional
    fun save(userId: UUID, request: PostRequest): Long {
        // save 'post' in db
        val post = Post(
            scope = request.scope.let { PostScope.valueOf(it!!) },
            content = request.content!!,
            userId = userId
        )
        val postId = postRepository.save(post).id!!

        // set 'like count' 0 in redis
        val likeCountKey = redisKeyProvider.likeCount()
        redisCommands.zAdd(key = likeCountKey, value = postId.toString(), score = 0.0)

        // add feed
        if (post.scope != PostScope.PRIVATE)
            feedEventHandler.whenAddPost(authorId = userId, postId = postId)

        return postId
    }

    fun like(userId: UUID, postId: Long, isUnlike: Boolean = false) {
        // settings
        val likeKey = redisKeyProvider.like(userId = userId)
        val likeCountKey = redisKeyProvider.likeCount()
        val now = System.currentTimeMillis().toDouble()

        // validate
        isNotAlreadyLiked(likeKey = likeKey, postId = postId, isUnlike = isUnlike)

        // save or delete
        if (isUnlike) {
            redisCommands.zRem(key = likeKey, value = postId.toString())
            redisCommands.zInc(key = likeCountKey, value = postId.toString(), delta = -1.0)
        } else {
            redisCommands.zAdd(key = likeKey, value = postId.toString(), score = now)
            redisCommands.zInc(key = likeCountKey, value = postId.toString(), delta = 1.0)
        }
    }

    @Transactional
    fun update(userId: UUID, postId: Long, request: PostRequest) {
        // get
        val post = get(postId = postId)

        // validate
        isAuthorOfPost(userId = userId, post = post)

        // update
        post.update(request = request)
    }

    @Transactional
    fun delete(userId: UUID, postId: Long) {
        // get
        val post = get(postId = postId)

        // validate
        isAuthorOfPost(userId = userId, post = post)

        // soft-delete
        post.delete()
    }
}
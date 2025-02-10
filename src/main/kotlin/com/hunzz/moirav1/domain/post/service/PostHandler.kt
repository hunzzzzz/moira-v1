package com.hunzz.moirav1.domain.post.service

import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.Post
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.post.repository.PostRepository
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.moirav1.global.exception.ErrorMessages.POST_NOT_FOUND
import com.hunzz.moirav1.global.exception.custom.InvalidPostInfoException
import org.springframework.aop.framework.AopContext
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PostHandler(
    private val postRepository: PostRepository
) {
    private fun proxy() = AopContext.currentProxy() as PostHandler

    private fun isAuthorOfPost(userId: UUID, post: Post) {
        val condition = userId == post.userId

        require(condition) { throw InvalidPostInfoException(CANNOT_UPDATE_OTHERS_POST) }
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

        return postRepository.save(post).id!!
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
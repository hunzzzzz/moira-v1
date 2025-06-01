package com.hunzz.common.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.exception.ErrorCode.POST_NOT_FOUND
import com.hunzz.common.exception.custom.InvalidPostInfoException
import com.hunzz.common.model.cache.PostInfo
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.PostRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class PostCacheManager(
    private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private fun getPostCacheFromRedis(postId: UUID): PostInfo? {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        return redisTemplate.opsForValue().get(postCacheKey)
            ?.let { objectMapper.readValue(it, PostInfo::class.java) }
    }

    private fun setPostCacheIntoRedis(postInfo: PostInfo) {
        val postCacheKey = redisKeyProvider.post(postId = postInfo.postId)

        redisTemplate.opsForValue().set(
            postCacheKey,
            objectMapper.writeValueAsString(postInfo),
            3,
            TimeUnit.DAYS
        )
    }

    private fun deletePostCacheFromRedis(postId: UUID) {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        redisTemplate.delete(postCacheKey)
    }

    fun get(postId: UUID): PostInfo {
        val postInfo = postRepository.findPostInfo(postId = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return postInfo
    }

    fun getWithRedisCache(postId: UUID): PostInfo {
        val postInfo = getPostCacheFromRedis(postId = postId)

        if (postInfo != null)
            return postInfo
        else {
            val postInfoFromDB = get(postId = postId)
            setPostCacheIntoRedis(postInfo = postInfoFromDB)

            return postInfoFromDB
        }
    }

    @Cacheable(cacheNames = ["post"], key = "#postId", cacheManager = "localCacheManager")
    fun getWithLocalCache(postId: UUID): PostInfo {
        return getWithRedisCache(postId = postId)
    }

    @CacheEvict(cacheNames = ["post"], key = "#postId", cacheManager = "localCacheManager")
    fun evictLocalCache(postId: UUID) {
        deletePostCacheFromRedis(postId = postId)
    }

    // ------------------------------------------------------------------------------------------

    private fun getPostAuthorCacheFromRedis(postId: UUID): UUID? {
        val postAuthorKey = redisKeyProvider.postAuthor(postId = postId)

        return redisTemplate.opsForValue().get(postAuthorKey)
            ?.let { UUID.fromString(it) }
    }

    private fun setPostAuthorCacheIntoRedis(postId: UUID, authorId: UUID) {
        val postAuthorKey = redisKeyProvider.postAuthor(postId = postId)

        redisTemplate.opsForValue().set(
            postAuthorKey,
            authorId.toString(),
            3,
            TimeUnit.DAYS
        )
    }

    fun getAuthorId(postId: UUID): UUID {
        val authorId = postRepository.findPostAuthorId(postId = postId)
            ?: throw InvalidPostInfoException(POST_NOT_FOUND)

        return authorId
    }

    fun getAuthorIdWithRedisCache(postId: UUID): UUID {
        val authorId = getPostAuthorCacheFromRedis(postId = postId)

        if (authorId != null)
            return authorId
        else {
            val authorIdFromDB = getAuthorId(postId = postId)

            setPostAuthorCacheIntoRedis(postId = postId, authorId = authorIdFromDB)

            return authorIdFromDB
        }
    }

    @Cacheable(cacheNames = ["post-author"], key = "#postId", cacheManager = "localCacheManager")
    fun getAuthorIdWithLocalCache(postId: UUID): UUID {
        return getAuthorIdWithRedisCache(postId = postId)
    }
}
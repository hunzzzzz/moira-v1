package com.hunzz.api.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.domain.dto.client.CommentInfo
import com.hunzz.api.domain.dto.client.PostInfo
import com.hunzz.api.utility.client.PostServerClient
import com.hunzz.api.utility.exception.ErrorCode.INTERNAL_SYSTEM_ERROR
import com.hunzz.api.utility.exception.InternalSystemException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class PostInfoProvider(
    private val objectMapper: ObjectMapper,
    private val postServerClient: PostServerClient,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getPostInfo(postId: Long): PostInfo {
        val postCacheKey = "post:$postId"
        val postInfo = redisTemplate.opsForValue().get(postCacheKey)

        if (postInfo != null)
            return objectMapper.readValue(postInfo, PostInfo::class.java)
        else {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    return postServerClient.getPostInfo(postId = postId)
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount == maxRetries) throw e

                    Thread.sleep(1000)
                }
            }
        }

        logger.error("[Error] notification -> post 서버 간 통신 에러가 발생하였습니다.")

        throw InternalSystemException(INTERNAL_SYSTEM_ERROR)
    }

    fun getCommentInfo(commentId: Long): CommentInfo {
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                return postServerClient.getCommentInfo(commentId = commentId)
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) throw e

                Thread.sleep(1000)
            }
        }

        logger.error("[Error] notification -> post 서버 간 통신 에러가 발생하였습니다.")

        throw InternalSystemException(INTERNAL_SYSTEM_ERROR)
    }
}
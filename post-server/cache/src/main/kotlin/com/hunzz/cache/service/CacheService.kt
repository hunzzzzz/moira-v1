package com.hunzz.cache.service

import com.hunzz.common.cache.PostCacheManager
import com.hunzz.common.repository.PostRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class CacheService(
    private val postCacheManager: PostCacheManager,
    private val postRepository: PostRepository
) {
    fun getPostAuthorId(postId: UUID): UUID {
        return postCacheManager.getAuthorIdWithLocalCache(postId = postId)
    }

    fun getLatestPostIds(authorId: UUID): List<UUID> {
        return postRepository.getLatestPosts(userId = authorId)
    }
}
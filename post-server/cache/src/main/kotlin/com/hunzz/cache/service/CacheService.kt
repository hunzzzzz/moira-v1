package com.hunzz.cache.service

import com.hunzz.common.cache.PostCacheManager
import org.springframework.stereotype.Component
import java.util.*

@Component
class CacheService(
    private val postCacheManager: PostCacheManager
) {
    fun getPostAuthorId(postId: UUID): UUID {
        return postCacheManager.getAuthorIdWithLocalCache(postId = postId)
    }
}
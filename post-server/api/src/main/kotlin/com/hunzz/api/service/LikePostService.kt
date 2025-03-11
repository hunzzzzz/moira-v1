package com.hunzz.api.service

import com.hunzz.api.cache.PostCache
import com.hunzz.api.cache.UserCache
import com.hunzz.api.component.PostRedisHandler
import com.hunzz.common.model.property.PostLikeType
import org.springframework.stereotype.Service
import java.util.*

@Service
class LikePostService(
    private val postRedisHandler: PostRedisHandler
) {
    @PostCache
    @UserCache
    fun like(userId: UUID, postId: UUID) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.LIKE)
    }

    fun unlike(userId: UUID, postId: UUID) {
        postRedisHandler.like(userId = userId, postId = postId, type = PostLikeType.UNLIKE)
    }
}
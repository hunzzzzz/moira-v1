package com.hunzz.common.repository

import com.hunzz.common.model.cache.PostInfo
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostCustomRepository {
    fun findPostInfo(
        postId: UUID
    ): PostInfo?
}
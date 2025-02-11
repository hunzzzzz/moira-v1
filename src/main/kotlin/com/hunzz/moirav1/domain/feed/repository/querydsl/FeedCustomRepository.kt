package com.hunzz.moirav1.domain.feed.repository.querydsl

import com.hunzz.moirav1.domain.feed.dto.response.FeedQueryDslResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedCustomRepository {
    fun findPosts(
        pageable: Pageable,
        userId: UUID,
        cursor: Long?
    ): List<FeedQueryDslResponse>
}
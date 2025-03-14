package com.hunzz.feedserver.domain.feed.repository.querydsl

import com.hunzz.feedserver.domain.feed.dto.response.querydsl.FeedQueryDslResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedCustomRepository {
    fun getFeed(
        pageable: Pageable,
        userId: UUID,
        cursor: Long?
    ): List<FeedQueryDslResponse>
}
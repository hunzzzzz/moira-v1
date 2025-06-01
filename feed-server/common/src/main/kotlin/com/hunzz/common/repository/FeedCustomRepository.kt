package com.hunzz.common.repository

import com.hunzz.common.querydsl.dto.QueryDslFeedResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedCustomRepository {
    fun getFeed(
        pageable: Pageable,
        userId: UUID,
        cursor: Long?
    ): List<QueryDslFeedResponse>
}
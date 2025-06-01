package com.hunzz.common.repository

import com.hunzz.common.model.QFeed
import com.hunzz.common.querydsl.dto.QueryDslFeedResponse
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class FeedRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : FeedCustomRepository {
    private val feed = QFeed.feed

    override fun getFeed(
        pageable: Pageable,
        userId: UUID,
        cursor: Long?
    ): List<QueryDslFeedResponse> {
        val conditions = BooleanBuilder().apply {
            feed.userId.eq(userId)
            cursor?.let { cursor -> and(feed.id.lt(cursor)) }
        }

        return jpaQueryFactory.select(
            Projections.constructor(
                QueryDslFeedResponse::class.java,
                feed.id,
                feed.postId,
                feed.authorId
            )
        ).from(feed)
            .where(conditions)
            .orderBy(feed.id.desc())
            .limit(pageable.pageSize.toLong())
            .fetch()
    }
}
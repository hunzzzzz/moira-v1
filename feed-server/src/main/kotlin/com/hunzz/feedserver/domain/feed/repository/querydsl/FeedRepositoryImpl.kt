package com.hunzz.feedserver.domain.feed.repository.querydsl

import com.hunzz.feedserver.domain.feed.dto.response.querydsl.FeedQueryDslResponse
import com.hunzz.feedserver.domain.feed.model.QFeed
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

    override fun getFeed(pageable: Pageable, userId: UUID, cursor: Long?): List<FeedQueryDslResponse> {
        val conditions = BooleanBuilder().apply {
            and(feed.userId.eq(userId))
            cursor?.let { cursor -> and(feed.postId.lt(cursor)) }
        }

        return jpaQueryFactory.select(
            Projections.constructor(
                FeedQueryDslResponse::class.java,
                feed.userId,
                feed.postId,
                feed.authorId
            )
        ).from(feed)
            .where(conditions)
            .orderBy(feed.postId.desc())
            .limit(pageable.pageSize.toLong())
            .fetch()
    }
}
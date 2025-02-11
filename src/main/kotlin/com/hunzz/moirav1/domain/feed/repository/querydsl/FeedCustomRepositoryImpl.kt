package com.hunzz.moirav1.domain.feed.repository.querydsl

import com.hunzz.moirav1.domain.feed.dto.response.FeedQueryDslResponse
import com.hunzz.moirav1.domain.feed.model.QFeed
import com.hunzz.moirav1.domain.post.model.PostStatus
import com.hunzz.moirav1.domain.post.model.QPost
import com.hunzz.moirav1.domain.user.model.QUser
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class FeedCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : FeedCustomRepository {
    private val feed = QFeed.feed
    private val post = QPost.post
    private val user = QUser.user

    override fun findPosts(pageable: Pageable, userId: UUID, cursor: Long?): List<FeedQueryDslResponse> {
        val conditions = BooleanBuilder().apply {
            and(feed.userId.eq(userId))
            and(post.status.ne(PostStatus.DELETED))
            cursor?.let { cursor -> and(post.id.lt(cursor)) }
        }

        return jpaQueryFactory.select(
            Projections.constructor(
                FeedQueryDslResponse::class.java,
                post.id,
                user.id,
                user.name,
                user.imageUrl,
                post.scope,
                post.content
            )
        ).from(feed)
            .join(post).on(feed.postId.eq(post.id))
            .join(user).on(feed.authorId.eq(user.id))
            .where(conditions)
            .orderBy(post.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }
}
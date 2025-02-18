package com.hunzz.postserver.domain.comment.repository.querydsl

import com.hunzz.postserver.domain.comment.dto.response.CommentQueryDslResponse
import com.hunzz.postserver.domain.comment.model.CommentStatus
import com.hunzz.postserver.domain.comment.model.QComment
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class CommentRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : CommentCustomRepository {
    private val comment = QComment.comment

    override fun getComments(
        pageable: Pageable,
        postId: Long,
        cursor: Long?
    ): List<CommentQueryDslResponse> {
        val conditions = BooleanBuilder().apply {
            comment.postId.eq(postId)
            comment.status.eq(CommentStatus.NORMAL)
            cursor?.let { cursor -> and(comment.id.lt(cursor)) }
        }

        val contents = jpaQueryFactory.select(
            Projections.constructor(
                CommentQueryDslResponse::class.java,
                comment.id,
                comment.status,
                comment.content,
                comment.userId
            )
        ).from(comment)
            .where(conditions)
            .orderBy(comment.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return contents
    }
}
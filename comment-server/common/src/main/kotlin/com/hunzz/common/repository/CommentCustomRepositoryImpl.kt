package com.hunzz.common.repository

import com.hunzz.common.model.QComment
import com.hunzz.common.model.property.CommentStatus
import com.hunzz.common.querydsl.dto.QueryDslCommentResponse
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class CommentCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : CommentCustomRepository {
    private val comment = QComment.comment

    override fun getComments(
        pageable: Pageable,
        postId: UUID,
        cursor: UUID?
    ): List<QueryDslCommentResponse> {
        // 커서로 들어온 commentId(UUID)에 해당하는 Comment의 createdAt을 불러온다.
        val cursorTime = cursor?.let {
            jpaQueryFactory.select(comment.createdAt).from(comment)
                .where(comment.id.eq(cursor)).fetchOne()
        } ?: LocalDateTime.now()

        // 조건절
        val conditions = BooleanBuilder().apply {
            and(comment.postId.eq(postId))
            and(comment.status.ne(CommentStatus.DELETED))
            cursor?.let { and(comment.createdAt.lt(cursorTime)) }
        }

        // 조회
        val contents = jpaQueryFactory.select(
            Projections.constructor(
                QueryDslCommentResponse::class.java,
                comment.id,
                comment.content,
                comment.createdAt,
                comment.userId
            )
        ).from(comment)
            .where(conditions)
            .orderBy(comment.id.asc())
            .limit(pageable.pageSize.toLong())
            .fetch()

        return contents
    }
}
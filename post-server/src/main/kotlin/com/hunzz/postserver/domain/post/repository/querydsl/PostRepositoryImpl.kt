package com.hunzz.postserver.domain.post.repository.querydsl

import com.hunzz.postserver.domain.post.model.PostScope
import com.hunzz.postserver.domain.post.model.PostStatus
import com.hunzz.postserver.domain.post.model.QPost
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PostRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostCustomRepository {
    private val post = QPost.post

    override fun getAllIds(userId: UUID): List<Long> {
        val conditions = BooleanBuilder().apply {
            and(post.userId.eq(userId))
            and(post.status.eq(PostStatus.NORMAL))
            and(post.scope.ne(PostScope.PRIVATE))
        }

        val contents = jpaQueryFactory.select(post.id)
            .from(post)
            .where(conditions)
            .orderBy(post.id.desc())
            .limit(10)
            .fetch()

        return contents
    }
}
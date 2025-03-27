package com.hunzz.common.repository

import com.hunzz.common.model.QPost
import com.hunzz.common.model.cache.PostInfo
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PostCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostCustomRepository {
    private val post = QPost.post

    override fun findPostInfo(postId: UUID): PostInfo? {
        return jpaQueryFactory.select(
            Projections.constructor(
                PostInfo::class.java,
                post.id,
                post.scope,
                post.status,
                post.content,
                post.imageUrls,
                post.thumbnailUrl
            )
        ).from(post).fetchOne()
    }
}
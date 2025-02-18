package com.hunzz.postserver.domain.comment.repository.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class CommentRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) {
}
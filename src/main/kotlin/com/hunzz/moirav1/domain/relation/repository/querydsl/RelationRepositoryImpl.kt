package com.hunzz.moirav1.domain.relation.repository.querydsl

import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import com.hunzz.moirav1.domain.relation.model.QRelation
import com.hunzz.moirav1.domain.relation.model.RelationType
import com.hunzz.moirav1.domain.user.model.QUser
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class RelationRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : RelationCustomRepository {
    private val relation = QRelation.relation
    private val user = QUser.user

    override fun getRelations(
        pageable: Pageable,
        userId: UUID,
        cursor: LocalDateTime?,
        type: RelationType
    ): Slice<FollowResponse> {
        val conditions = BooleanBuilder().apply {
            // condition1 : set target
            // 내 팔로잉 목록을 찾으려면, relation의 userId가 userId(나)와 동일해야 한다.
            // 내 팔로워 목록을 찾으려면, relation의 targetId가 userId(나)와 동일해야 한다.
            when (type) {
                RelationType.FOLLOWING -> and(relation.userId.eq(userId))
                RelationType.FOLLOWER -> and(relation.targetId.eq(userId))
            }
            // condition2: cursor (createdAt)
            cursor?.let { cursor -> and(relation.createdAt.lt(cursor)) }
        }

        val contents = jpaQueryFactory.select(
            Projections.constructor(
                FollowResponse::class.java,
                user.id,
                user.name,
                user.imageUrl,
                relation.createdAt
            )
        ).from(relation)
            .join(user)
            .on(
                // 내 팔로잉 목록을 찾으려면, relation의 targetId를 조회해야 한다.
                // 내 팔로워 목록을 찾으려면, relation의 userId를 조회해야 한다.
                when (type) {
                    RelationType.FOLLOWING -> relation.targetId.eq(user.id)
                    RelationType.FOLLOWER -> relation.userId.eq(user.id)
                }
            )
            .where(conditions)
            .orderBy(relation.createdAt.desc())
            .limit(pageable.pageSize.toLong())
            .fetch()

        return SliceImpl(contents, pageable, contents.size > pageable.pageSize)
    }
}
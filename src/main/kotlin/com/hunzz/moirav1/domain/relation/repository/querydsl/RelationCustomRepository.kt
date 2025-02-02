package com.hunzz.moirav1.domain.relation.repository.querydsl

import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import com.hunzz.moirav1.domain.relation.model.RelationType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RelationCustomRepository {
    fun getRelations(
        pageable: Pageable,
        userId: UUID,
        cursor: LocalDateTime?,
        type: RelationType
    ): Slice<FollowResponse>
}
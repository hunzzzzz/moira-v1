package com.hunzz.relationserver.domain.repository

import com.hunzz.relationserver.domain.dto.response.FollowResponse
import com.hunzz.relationserver.domain.model.RelationType
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RelationCustomRepository {
    fun findRelations(
        userId: UUID,
        type: RelationType,
        cursor: UUID?
    ): List<FollowResponse>
}
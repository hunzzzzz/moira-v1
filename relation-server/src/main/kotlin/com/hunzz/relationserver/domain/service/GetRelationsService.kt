package com.hunzz.relationserver.domain.service

import com.hunzz.relationserver.domain.component.RelationRedisHandler
import com.hunzz.relationserver.domain.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.model.RelationType
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetRelationsService(
    private val relationRedisHandler: RelationRedisHandler
) {
    companion object {
        const val RELATION_PAGE_SIZE = 10L
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType): FollowSliceResponse {
        // Redis에서 팔로잉 혹은 팔로워 관련 정보 조회
        val followResponses = relationRedisHandler.getRelations(
            userId = userId,
            cursor = cursor,
            type = type,
            pageSize = RELATION_PAGE_SIZE
        )

        // 커서 정보 추가
        return FollowSliceResponse(
            currentCursor = cursor,
            nextCursor = if (followResponses.size <= RELATION_PAGE_SIZE) null else followResponses.lastOrNull()?.userId,
            contents = followResponses
        )
    }
}
package com.hunzz.relationserver.domain.relation.service

import com.hunzz.relationserver.domain.relation.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.relation.model.RelationType
import com.hunzz.relationserver.global.exception.ErrorCode.CANNOT_FOLLOW_ITSELF
import com.hunzz.relationserver.global.exception.ErrorCode.CANNOT_UNFOLLOW_ITSELF
import com.hunzz.relationserver.global.exception.custom.InvalidRelationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class RelationHandler(
    private val relationRedisScriptHandler: RelationRedisScriptHandler
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val RELATION_PAGE_SIZE = 10L
    }

    @Transactional
    fun follow(userId: UUID, targetId: UUID) {
        // validate
        if (userId == targetId) throw InvalidRelationException(CANNOT_FOLLOW_ITSELF)

        relationRedisScriptHandler.checkFollowRequest(userId = userId, targetId = targetId)

        // save relation info in redis
        relationRedisScriptHandler.follow(userId = userId, targetId = targetId)
    }

    @Transactional
    fun unfollow(userId: UUID, targetId: UUID) {
        // validate
        if (userId == targetId) throw InvalidRelationException(CANNOT_UNFOLLOW_ITSELF)

        relationRedisScriptHandler.checkUnfollowRequest(userId = userId, targetId = targetId)

        // delete relation info from redis
        relationRedisScriptHandler.unfollow(userId = userId, targetId = targetId)
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType): FollowSliceResponse {
        val followResponses = relationRedisScriptHandler.getRelations(
            userId = userId,
            cursor = cursor,
            type = type,
            pageSize = RELATION_PAGE_SIZE
        )

        return FollowSliceResponse(
            currentCursor = cursor,
            nextCursor = followResponses.lastOrNull()?.userId,
            contents = followResponses
        )
    }
}
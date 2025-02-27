package com.hunzz.relationserver.domain.relation.service

import com.hunzz.relationserver.domain.relation.dto.request.KafkaFollowRequest
import com.hunzz.relationserver.domain.relation.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.relation.model.RelationType
import com.hunzz.relationserver.global.exception.ErrorCode.CANNOT_FOLLOW_ITSELF
import com.hunzz.relationserver.global.exception.ErrorCode.CANNOT_UNFOLLOW_ITSELF
import com.hunzz.relationserver.global.exception.custom.InvalidRelationException
import com.hunzz.relationserver.global.utility.KafkaProducer
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class RelationHandler(
    private val kafkaProducer: KafkaProducer,
    private val relationRedisScriptHandler: RelationRedisScriptHandler
) {
    companion object {
        const val RELATION_PAGE_SIZE = 10L
    }

    @Transactional
    fun follow(userId: UUID, targetId: UUID) {
        // 검증
        if (userId == targetId) throw InvalidRelationException(CANNOT_FOLLOW_ITSELF)
        relationRedisScriptHandler.checkFollowRequest(userId = userId, targetId = targetId)

        // Redis에 팔로우 정보 저장
        relationRedisScriptHandler.follow(userId = userId, targetId = targetId)

        // Kafka 메시지 전송 (to 피드 서버, 알림 서버)
        kafkaProducer.send(
            topic = "follow",
            data = KafkaFollowRequest(userId = userId, targetId = targetId)
        )
    }

    @Transactional
    fun unfollow(userId: UUID, targetId: UUID) {
        // validate
        if (userId == targetId) throw InvalidRelationException(CANNOT_UNFOLLOW_ITSELF)

        relationRedisScriptHandler.checkUnfollowRequest(userId = userId, targetId = targetId)

        // delete relation info from redis
        relationRedisScriptHandler.unfollow(userId = userId, targetId = targetId)

        // send kafka-message (to feed-server)
        val data = KafkaFollowRequest(userId = userId, targetId = targetId)
        kafkaProducer.send(topic = "unfollow", data = data)
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType): FollowSliceResponse {
        // get relation infos from redis
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
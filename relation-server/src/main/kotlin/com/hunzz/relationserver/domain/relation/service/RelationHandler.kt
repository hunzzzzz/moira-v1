package com.hunzz.relationserver.domain.relation.service

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
    @Transactional
    fun follow(userId: UUID, targetId: UUID) {
        // validate
        if (userId == targetId) throw InvalidRelationException(CANNOT_FOLLOW_ITSELF)

        relationRedisScriptHandler.checkFollowRequest(userId = userId, targetId = targetId)

        // send kafka message (redis command)
        kafkaProducer.send("follow", mapOf("userId" to userId, "targetId" to targetId))
    }

    @Transactional
    fun unfollow(userId: UUID, targetId: UUID) {
        // validate
        if (userId == targetId) throw InvalidRelationException(CANNOT_UNFOLLOW_ITSELF)

        relationRedisScriptHandler.checkUnfollowRequest(userId = userId, targetId = targetId)

        // send kafka message (redis command)
        kafkaProducer.send("unfollow", mapOf("userId" to userId, "targetId" to targetId))
    }
}
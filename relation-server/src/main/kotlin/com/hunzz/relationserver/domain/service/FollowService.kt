package com.hunzz.relationserver.domain.service

import com.hunzz.relationserver.domain.component.RelationKafkaHandler
import com.hunzz.relationserver.domain.component.RelationRedisHandler
import org.springframework.stereotype.Service
import java.util.*

@Service
class FollowService(
    private val relationKafkaHandler: RelationKafkaHandler,
    private val relationRedisHandler: RelationRedisHandler
) {
    fun follow(userId: UUID, targetId: UUID) {
        // 검증
        relationRedisHandler.checkFollowRequest(userId = userId, targetId = targetId)

        // Redis에 팔로우 정보 저장
        relationRedisHandler.follow(userId = userId, targetId = targetId)

        // Kafka 메시지 전송 (relation -> feed)
        relationKafkaHandler.follow(userId = userId, targetId = targetId)
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        // 검증
        relationRedisHandler.checkUnfollowRequest(userId = userId, targetId = targetId)

        // Redis에 팔로우 정보 삭제
        relationRedisHandler.unfollow(userId = userId, targetId = targetId)

        // Kafka 메시지 전송 (relation -> feed)
        relationKafkaHandler.unfollow(userId = userId, targetId = targetId)
    }
}
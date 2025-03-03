package com.hunzz.relationserver.domain.component

import com.hunzz.relationserver.utility.kafka.KafkaProducer
import com.hunzz.relationserver.utility.kafka.dto.FollowKafkaRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class RelationKafkaHandler(
    private val kafkaProducer: KafkaProducer
) {
    fun follow(userId: UUID, targetId: UUID) {
        val data = FollowKafkaRequest(userId = userId, targetId = targetId)

        kafkaProducer.send(topic = "follow", data = data)
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        val data = FollowKafkaRequest(userId = userId, targetId = targetId)

        kafkaProducer.send(topic = "unfollow", data = data)
    }
}
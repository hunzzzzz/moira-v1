package com.hunzz.relationserver.domain.component

import com.hunzz.relationserver.utility.kafka.KafkaProducer
import com.hunzz.relationserver.utility.kafka.dto.KafkaUpdateFeedRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class RelationKafkaHandler(
    private val kafkaProducer: KafkaProducer
) {
    fun follow(userId: UUID, targetId: UUID) {
        val data = KafkaUpdateFeedRequest(userId = userId, authorId = targetId)

        kafkaProducer.send(topic = "update-feed-when-follow", data = data)
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        val data = KafkaUpdateFeedRequest(userId = userId, authorId = targetId)

        kafkaProducer.send(topic = "delete-feed-when-unfollow", data = data)
    }
}
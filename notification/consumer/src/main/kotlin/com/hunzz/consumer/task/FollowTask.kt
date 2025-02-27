package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.model.FollowNotification
import com.hunzz.common.repository.NotificationRepository
import com.hunzz.consumer.dto.KafkaFollowRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class FollowTask(
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(topics = ["follow"], groupId = "notification-server-follow")
    fun follow(message: String) {
        val data = objectMapper.readValue(message, KafkaFollowRequest::class.java)

        notificationRepository.save(
            FollowNotification(
                actorId = data.userId,
                targetId = data.targetId
            )
        )
    }
}
package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.model.LikeNotification
import com.hunzz.common.model.NotificationType
import com.hunzz.common.repository.NotificationRepository
import com.hunzz.consumer.dto.KafkaLikeRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeTask(
    private val objectMapper: ObjectMapper,
    private val notificationRepository: NotificationRepository
) {
    @KafkaListener(topics = ["like-notification"], groupId = "notification-server-like")
    @Transactional
    fun like(message: String) {
        val data = objectMapper.readValue(message, KafkaLikeRequest::class.java)
        val notification = notificationRepository.findByTypeAndPostId(
            type = NotificationType.LIKE,
            postId = data.postId
        )

        // 이미 발생한 좋아요 알림이 존재하는 경우, numOfLikes 필드 값만 수정한다.
        if (notification != null)
            (notification as LikeNotification).update(newUserIds = data.userIds)
        // 좋아요 알림이 처음 발생한 경우, Notification 객체를 저장한다.
        else
            notificationRepository.save(
                LikeNotification(
                    postAuthorId = data.postAuthorId,
                    postId = data.postId,
                    userIds = data.userIds
                )
            )
    }
}
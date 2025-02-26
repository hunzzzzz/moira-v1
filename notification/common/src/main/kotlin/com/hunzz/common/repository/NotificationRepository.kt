package com.hunzz.common.repository

import com.hunzz.common.model.Notification
import com.hunzz.common.model.NotificationType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : MongoRepository<Notification, String> {
    @Query("{ 'type': ?0, 'postId': ?1 }")
    fun findByTypeAndPostId(type: NotificationType, postId: Long): Notification?
}
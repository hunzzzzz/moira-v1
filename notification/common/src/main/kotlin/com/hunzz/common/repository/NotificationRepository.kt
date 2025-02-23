package com.hunzz.common.repository

import com.hunzz.common.model.Notification
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : MongoRepository<Notification, String> {
    fun id(id: String): MutableList<Notification>
}
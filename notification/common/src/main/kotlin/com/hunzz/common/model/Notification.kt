package com.hunzz.common.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "notifications")
abstract class Notification(
    @Id
    val id: String = ObjectId().toString(),
    val userId: String,
    val type: NotificationType,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime = LocalDateTime.now().plusDays(90)
)
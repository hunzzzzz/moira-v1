package com.hunzz.common.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias

@TypeAlias("FollowNotification")
data class FollowNotification(
    override val id: ObjectId = ObjectId(),
    val actorId: String,
    val targetId: String
) : Notification(id = id, userId = actorId, type = NotificationType.FOLLOW)
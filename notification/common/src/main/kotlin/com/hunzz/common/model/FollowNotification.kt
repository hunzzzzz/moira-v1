package com.hunzz.common.model

import org.springframework.data.annotation.TypeAlias

@TypeAlias("FollowNotification")
data class FollowNotification(
    val actorId: String,
    val targetId: String
) : Notification(userId = actorId, type = NotificationType.FOLLOW)
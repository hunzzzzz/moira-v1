package com.hunzz.api.domain.dto.response

import com.hunzz.common.model.NotificationType
import java.time.LocalDateTime

data class FollowNotificationResponse(
    override val id: String,
    override val type: NotificationType,
    override val createdAt: LocalDateTime,

    val userName: String,
    val userImageUrl: String?,
    val isFollowing: Boolean
) : NotificationResponse(id = id, type = type, createdAt = createdAt)

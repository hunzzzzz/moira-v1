package com.hunzz.api.domain.dto.response

import com.hunzz.common.model.NotificationType
import java.time.LocalDateTime

data class CommentNotificationResponse(
    override val id: String,
    override val type: NotificationType,
    override val createdAt: LocalDateTime,

    val userId: String,
    val userName: String,
    val userImageUrl: String?,

    val postId: Long,
    val postImageUrl: String?,

    val commentContent: String
) : NotificationResponse(id = id, type = type, createdAt = createdAt)

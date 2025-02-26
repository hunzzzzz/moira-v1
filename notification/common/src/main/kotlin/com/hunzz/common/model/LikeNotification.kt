package com.hunzz.common.model

import org.springframework.data.annotation.TypeAlias

@TypeAlias("LikeNotification")
data class LikeNotification(
    val postAuthorId: String,
    val postId: Long,
    val userIds: MutableList<String>
) : Notification(userId = postAuthorId, type = NotificationType.LIKE) {

    fun update(newUserIds: List<String>) {
        userIds.addAll(newUserIds)
    }
}
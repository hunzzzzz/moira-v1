package com.hunzz.common.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias

@TypeAlias("LikeNotification")
data class LikeNotification(
    override val id: ObjectId = ObjectId(),
    val postAuthorId: String,
    val postId: Long,
    val userIds: MutableList<String>
) : Notification(id = id, userId = postAuthorId, type = NotificationType.LIKE) {

    fun update(newUserIds: List<String>) {
        userIds.addAll(newUserIds)
    }
}
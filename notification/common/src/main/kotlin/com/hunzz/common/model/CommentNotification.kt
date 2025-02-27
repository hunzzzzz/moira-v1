package com.hunzz.common.model

import org.bson.types.ObjectId

data class CommentNotification(
    override val id: ObjectId = ObjectId(),
    val postAuthorId: String,
    val commentAuthorId: String,
    val postId: Long,
    val commentId: Long,
) : Notification(id = id, userId = postAuthorId, type = NotificationType.COMMENT)
package com.hunzz.common.model

data class CommentNotification(
    val postAuthorId: String,
    val commentAuthorId: String,
    val postId: Long,
    val commentId: Long,
) : Notification(userId = postAuthorId, type = NotificationType.COMMENT)
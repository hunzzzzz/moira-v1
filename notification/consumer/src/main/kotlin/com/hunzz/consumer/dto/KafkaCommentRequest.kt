package com.hunzz.consumer.dto

data class KafkaCommentRequest(
    val postAuthorId: String,
    val commentAuthorId: String,
    val postId: Long,
    val commentId: Long
)

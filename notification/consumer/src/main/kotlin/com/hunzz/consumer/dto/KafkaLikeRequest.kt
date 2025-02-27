package com.hunzz.consumer.dto

data class KafkaLikeRequest(
    var postAuthorId: String,
    val postId: Long,
    val userIds: MutableList<String>,
    val numOfLikes: Long
)

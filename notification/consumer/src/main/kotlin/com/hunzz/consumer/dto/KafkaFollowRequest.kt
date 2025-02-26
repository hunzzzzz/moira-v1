package com.hunzz.consumer.dto

data class KafkaFollowRequest(
    val userId: String,
    val targetId: String
)

package com.hunzz.postserver.domain.post.dto.request

data class KafkaLikeRequest(
    var postAuthorId: String,
    val postId: Long,
    val userIds: List<String>
)
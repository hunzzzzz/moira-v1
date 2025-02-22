package com.hunzz.postserver.domain.post.dto.request

data class KafkaImageRequest(
    val originalFileName: String,
    val thumbnailFileName: String,
    val image: ByteArray
)

package com.hunzz.userserver.dto.request

data class KafkaImageRequest(
    val originalFileName: String,
    val thumbnailFileName: String,
    val image: ByteArray
)

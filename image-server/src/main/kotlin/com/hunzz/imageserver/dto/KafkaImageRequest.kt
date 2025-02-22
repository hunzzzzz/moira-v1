package com.hunzz.imageserver.dto

data class KafkaImageRequest(
    val originalFileName: String,
    val thumbnailFileName: String,
    val image: ByteArray
)

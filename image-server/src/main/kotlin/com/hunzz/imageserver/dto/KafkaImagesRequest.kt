package com.hunzz.imageserver.dto

data class KafkaImagesRequest(
    val fileNames: List<String>,
    val thumbnailFileName: String,
    val images: List<ByteArray>
)

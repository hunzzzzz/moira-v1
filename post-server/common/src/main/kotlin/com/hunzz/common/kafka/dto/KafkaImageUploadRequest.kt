package com.hunzz.common.kafka.dto

data class KafkaImageUploadRequest(
    val originalFileName: String,
    val thumbnailFileName: String,
    val image: ByteArray
) {
    constructor() : this("", "", byteArrayOf())
}

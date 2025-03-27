package com.hunzz.common.kafka.dto

data class KafkaUploadImagesRequest(
    val fileNames: List<String>,
    val thumbnailFileName: String,
    val images: List<ByteArray>
) {
    constructor() : this(emptyList(), "", listOf())
}

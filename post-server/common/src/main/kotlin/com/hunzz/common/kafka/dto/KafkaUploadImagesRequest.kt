package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaUploadImagesRequest(
    val txId: UUID,
    val userId: UUID,
    val fileNames: List<String>,
    val thumbnailFileName: String,
    val images: List<ByteArray>
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), emptyList(), "", listOf())
}
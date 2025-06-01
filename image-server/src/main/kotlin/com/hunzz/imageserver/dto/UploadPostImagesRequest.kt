package com.hunzz.imageserver.dto

import java.util.*

data class UploadPostImagesRequest(
    val txId: UUID,
    val userId: UUID,
    val fileNames: List<String>,
    val thumbnailFileName: String,
    val images: List<ByteArray>
)

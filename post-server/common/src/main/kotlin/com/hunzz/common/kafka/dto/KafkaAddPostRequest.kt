package com.hunzz.common.kafka.dto

import com.hunzz.common.model.property.PostScope
import java.util.*

data class KafkaAddPostRequest(
    val txId: UUID,
    val postId: UUID,
    val content: String,
    val scope: PostScope,
    val userId: UUID,
    val imageUrls: List<String>?,
    val thumbnailUrl: String?
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), "", PostScope.PUBLIC, UUID.randomUUID(), null, null)
}

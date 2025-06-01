package com.hunzz.common.kafka.dto

import com.hunzz.common.model.property.PostScope
import java.util.*

data class KafkaUpdatePostRequest(
    val postId: UUID,
    val userId: UUID,
    val content: String,
    val scope: PostScope
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), "", PostScope.PUBLIC)
}

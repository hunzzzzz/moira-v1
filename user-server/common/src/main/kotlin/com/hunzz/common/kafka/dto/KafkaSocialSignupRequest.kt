package com.hunzz.common.kafka.dto

import java.util.*

data class KafkaSocialSignupRequest(
    val userId: UUID,
    val email: String,
    val name: String
) {
    constructor() : this(UUID.randomUUID(), "", "")
}

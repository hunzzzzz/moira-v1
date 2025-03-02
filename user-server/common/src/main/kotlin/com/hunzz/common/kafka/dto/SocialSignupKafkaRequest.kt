package com.hunzz.common.kafka.dto

import java.util.*

data class SocialSignupKafkaRequest(
    val userId: UUID,
    val email: String,
    val name: String
)

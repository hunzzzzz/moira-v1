package com.hunzz.authserver.utility.kafka.dto

import java.util.*

data class KafkaSocialSignupRequest(
    val userId: UUID,
    val email: String,
    val name: String
)

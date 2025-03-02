package com.hunzz.common.kafka.dto

import java.util.*

data class SignupKafkaRequest(
    val userId: UUID,
    var email: String,
    var password: String,
    var name: String,
    var adminCode: String?
) {
    constructor() : this(UUID.randomUUID(), "", "", "", null)
}

package com.hunzz.common.kafka.dto

data class KafkaSignupRequest(
    var email: String,
    var password: String,
    var name: String
) {
    constructor() : this("", "", "")
}

package com.hunzz.authserver.utility.idempotent

data class Idempotency(
    val key: String,
    val className: String,
    val response: String
)
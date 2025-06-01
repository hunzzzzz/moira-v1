package com.hunzz.authserver.utility.idempotent

data class Idempotency(
    val className: String,
    val response: String
)
package com.hunzz.authserver.utility.auth

import java.util.*

data class UserAuth(
    val userId: UUID,
    val type: String,
    val role: String,
    val email: String,
    val password: String?
) {
    constructor() : this(UUID.randomUUID(), "", "", "", "")
}
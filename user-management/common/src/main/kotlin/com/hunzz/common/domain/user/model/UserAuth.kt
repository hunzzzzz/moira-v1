package com.hunzz.common.domain.user.model

import java.util.*

data class UserAuth(
    val userId: UUID,
    val role: UserRole,
    val email: String,
    val password: String
) {
    constructor() : this(UUID.randomUUID(), UserRole.USER, "", "")
}
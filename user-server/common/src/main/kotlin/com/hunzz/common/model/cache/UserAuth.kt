package com.hunzz.common.model.cache

import com.hunzz.common.model.property.UserRole
import com.hunzz.common.model.property.UserType
import java.util.*

data class UserAuth(
    val userId: UUID,
    val type: UserType,
    val role: UserRole,
    val email: String,
    val password: String?
) {
    constructor() : this(UUID.randomUUID(), UserType.NORMAL, UserRole.USER, "", null)
}
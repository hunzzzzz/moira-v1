package com.hunzz.userserver.domain.user.dto.response

import com.hunzz.common.domain.user.model.UserStatus
import java.util.*

data class UserResponse(
    val id: UUID,
    val status: UserStatus,
    val name: String,
    val imageUrl: String?,
    val isMyProfile: Boolean,
)
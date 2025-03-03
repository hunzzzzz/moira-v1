package com.hunzz.api.dto.response

import com.hunzz.common.model.property.UserStatus
import java.util.*

data class UserResponse(
    val id: UUID,
    val status: UserStatus,
    val name: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val numOfFollowings: Long,
    val numOfFollowers: Long,
    val isMyProfile: Boolean,
)
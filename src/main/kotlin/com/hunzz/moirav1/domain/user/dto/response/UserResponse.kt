package com.hunzz.moirav1.domain.user.dto.response

import com.hunzz.moirav1.domain.user.model.User
import com.hunzz.moirav1.domain.user.model.UserStatus
import java.util.*

data class UserResponse(
    val id: UUID,
    val status: UserStatus,
    val email: String,
    val name: String,
    val imageUrl: String?,
    val isMyProfile: Boolean,
) {
    companion object {
        fun from(user: User, isMyProfile: Boolean) = UserResponse(
            id = user.id!!,
            status = user.status,
            email = user.email,
            name = user.name,
            imageUrl = user.imageUrl,
            isMyProfile = isMyProfile
        )
    }
}
package com.hunzz.userserver.domain.user.dto.response

data class UserRedisResponse(
    val numOfFollowings: Long,
    val numOfFollowers: Long
)
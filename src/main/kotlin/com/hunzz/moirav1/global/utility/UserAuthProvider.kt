package com.hunzz.moirav1.global.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.model.User
import com.hunzz.moirav1.domain.user.model.UserAuth
import org.springframework.stereotype.Component

@Component
class UserAuthProvider(
    private val objectMapper: ObjectMapper,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) {
    fun saveUserAuthInRedis(user: User): UserAuth {
        val userAuth = UserAuth(
            userId = user.id!!,
            role = user.role,
            email = user.email,
            password = user.password
        )

        val authKey = redisKeyProvider.userAuth(email = user.email)
        val data = objectMapper.writeValueAsString(userAuth)

        redisCommands.set(key = authKey, value = data)

        return userAuth
    }
}
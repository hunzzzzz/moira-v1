package com.hunzz.moirav1.global.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.model.User
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.domain.user.repository.UserRepository
import com.hunzz.moirav1.global.exception.ErrorMessages.USER_NOT_FOUND
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
import org.springframework.stereotype.Component

@Component
class UserAuthProvider(
    private val objectMapper: ObjectMapper,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userRepository: UserRepository
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

    fun getUserAuthFromRedis(email: String): UserAuth {
        val userAuthKey = redisKeyProvider.userAuth(email = email)
        val userAuth = redisCommands.get(key = userAuthKey)

        return if (userAuth != null)
            objectMapper.readValue(userAuth, UserAuth::class.java)
        else {
            val user = userRepository.findByEmail(email = email)
                ?: throw InvalidUserInfoException(USER_NOT_FOUND)
            UserAuth(
                userId = user.id!!,
                role = user.role,
                email = user.email,
                password = user.password
            )
        }
    }
}
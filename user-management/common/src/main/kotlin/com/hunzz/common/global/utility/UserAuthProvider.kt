package com.hunzz.common.global.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import org.springframework.stereotype.Component

@Component
class UserAuthProvider(
    private val objectMapper: ObjectMapper,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userRepository: UserRepository
) {
    fun getUserAuthFromRedis(email: String): UserAuth {
        val userAuthKey = redisKeyProvider.auth(email = email)
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
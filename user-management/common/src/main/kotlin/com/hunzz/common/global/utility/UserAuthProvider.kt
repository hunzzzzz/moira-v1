package com.hunzz.common.global.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class UserAuthProvider(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userRepository: UserRepository
) {
    private fun getAuthCache(email: String): UserAuth? {
        val authKey = redisKeyProvider.auth(email = email)

        return redisTemplate.opsForValue().get(authKey)
            ?.let { objectMapper.readValue(it, UserAuth::class.java) }
    }

    private fun setAuthCache(email: String, userAuth: UserAuth) {
        val authKey = redisKeyProvider.auth(email = email)

        redisTemplate.opsForValue().set(
            authKey,
            objectMapper.writeValueAsString(userAuth),
            3,
            TimeUnit.DAYS
        )
    }

    fun getUserAuth(email: String): UserAuth {
        return userRepository.findUserAuth(email = email)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)
    }

    fun getUserAuthWithRedisCache(email: String): UserAuth {
        val userAuth = getAuthCache(email = email)

        return if (userAuth == null) {
            val auth = getUserAuth(email = email)
            setAuthCache(email = email, userAuth = auth)

            auth
        } else userAuth
    }

    @Cacheable(cacheNames = ["auth"], key = "#email", cacheManager = "localCacheManager")
    fun getUserAuthWithLocalCache(email: String): UserAuth {
        return getUserAuthWithRedisCache(email = email)
    }
}
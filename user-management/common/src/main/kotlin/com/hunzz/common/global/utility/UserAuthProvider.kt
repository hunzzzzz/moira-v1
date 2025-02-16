package com.hunzz.common.global.utility

import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class UserAuthProvider(
    private val userRepository: UserRepository
) {
    fun getUserAuth(email: String): UserAuth {
        val user = userRepository.findByEmail(email = email)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return UserAuth(
            userId = user.id!!,
            role = user.role,
            email = user.email,
            password = user.password
        )
    }

    @Cacheable(cacheNames = ["auth"], key = "#email", cacheManager = "redisCacheManager")
    fun getUserAuthWithRedisCache(email: String): UserAuth {
        return getUserAuth(email = email)
    }

    @Cacheable(cacheNames = ["auth"], key = "#email", cacheManager = "localCacheManager")
    fun getUserAuthWithLocalCache(email: String): UserAuth {
        return getUserAuthWithRedisCache(email = email)
    }
}
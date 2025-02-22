package com.hunzz.userserver.utility

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserCacheManager(
    private val userRedisHandler: UserRedisHandler,
    private val userRepository: UserRepository
) {
    fun get(userId: UUID): CachedUser {
        val user = userRepository.findUserProfile(userId = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return CachedUser(
            userId = userId,
            status = user.status,
            name = user.name,
            imageUrl = user.imageUrl,
            thumbnailUrl = user.thumbnailUrl
        )
    }

    fun getWithRedisCache(userId: UUID): CachedUser {
        val userCache = userRedisHandler.getUserCache(userId = userId)

        if (userCache != null)
            return userCache
        else {
            val cachedUser = get(userId = userId)
            userRedisHandler.setUserCache(userId = userId, cachedUser = cachedUser)

            return cachedUser
        }
    }

    @Cacheable(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun getWithLocalCache(userId: UUID): CachedUser {
        return getWithRedisCache(userId = userId)
    }

    @CacheEvict(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun evictLocalCache(userId: UUID) {
    }
}
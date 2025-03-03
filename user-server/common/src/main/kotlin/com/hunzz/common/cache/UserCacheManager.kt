package com.hunzz.common.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.exception.custom.InvalidUserInfoException
import com.hunzz.common.model.cache.UserInfo
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class UserCacheManager(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userRepository: UserRepository
) {
    private fun getUserCacheFromRedis(userId: UUID): UserInfo? {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        return redisTemplate.opsForValue().get(userCacheKey)
            ?.let { objectMapper.readValue(it, UserInfo::class.java) }
    }

    private fun setUserCacheIntoRedis(userId: UUID, userInfo: UserInfo) {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        redisTemplate.opsForValue().set(
            userCacheKey,
            objectMapper.writeValueAsString(userInfo),
            3,
            TimeUnit.DAYS
        )
    }

    private fun deleteUserCacheFromRedis(userId: UUID) {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        redisTemplate.delete(userCacheKey)
    }

    fun get(userId: UUID): UserInfo {
        val user = userRepository.findUserProfile(userId = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return UserInfo(
            userId = userId,
            status = user.status,
            name = user.name,
            imageUrl = user.imageUrl,
            thumbnailUrl = user.thumbnailUrl
        )
    }

    fun getWithRedisCache(userId: UUID): UserInfo {
        val userInfo = getUserCacheFromRedis(userId = userId)

        if (userInfo != null)
            return userInfo
        else {
            val userInfoFromDB = get(userId = userId)
            setUserCacheIntoRedis(userId = userId, userInfo = userInfoFromDB)

            return userInfoFromDB
        }
    }

    @Cacheable(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun getWithLocalCache(userId: UUID): UserInfo {
        return getWithRedisCache(userId = userId)
    }

    @CacheEvict(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun evictLocalCache(userId: UUID) {
        deleteUserCacheFromRedis(userId = userId)
    }
}
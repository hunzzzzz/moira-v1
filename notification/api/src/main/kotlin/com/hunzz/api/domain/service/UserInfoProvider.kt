package com.hunzz.api.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.domain.dto.client.UserInfo
import com.hunzz.api.utility.client.UserServerClient
import com.hunzz.api.utility.exception.ErrorCode.INTERNAL_SYSTEM_ERROR
import com.hunzz.api.utility.exception.InternalSystemException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserInfoProvider(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getUserInfo(userId: String): UserInfo {
        // Redis에서 유저 캐시 조회
        val userCacheKey = "user:$userId"
        val userInfo = redisTemplate.opsForValue().get(userCacheKey)

        if (userInfo != null)
            return objectMapper.readValue(userInfo, UserInfo::class.java)
        // Redis에 유저 캐시가 존재하지 않으면, user-server에서 값을 직접 가져온다.
        else {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    return userServerClient.getUserInfo(userId = UUID.fromString(userId))
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount == maxRetries) throw e

                    Thread.sleep(1000)
                }
            }
        }

        logger.error("[Error] notification -> user 서버 간 통신 에러가 발생하였습니다.")

        throw InternalSystemException(INTERNAL_SYSTEM_ERROR)
    }
}
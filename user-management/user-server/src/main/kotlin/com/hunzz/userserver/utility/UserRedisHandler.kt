package com.hunzz.userserver.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.DUPLICATED_EMAIL
import com.hunzz.common.global.exception.ErrorCode.INVALID_ADMIN_CODE
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.RedisKeyProvider
import com.hunzz.userserver.dto.response.RelationInfo
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class UserRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun validateSignupRequest(inputEmail: String, inputAdminCode: String?) {
        // settings
        val script = redisScriptProvider.validateSignupRequest()
        val emailsKey = redisKeyProvider.emails()
        val adminCodeKey = redisKeyProvider.adminCode()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(emailsKey, adminCodeKey), // keys
            inputEmail, // argv[1]
            inputAdminCode ?: "" // argv[2]
        )

        // check result
        when (result) {
            DUPLICATED_EMAIL.name -> throw InvalidUserInfoException(DUPLICATED_EMAIL)
            INVALID_ADMIN_CODE.name -> throw InvalidAdminRequestException(INVALID_ADMIN_CODE)
        }
    }

    fun signup(userAuth: UserAuth) {
        // settings
        val script = redisScriptProvider.signup()
        val emailsKey = redisKeyProvider.emails()
        val idsKey = redisKeyProvider.ids()
        val authKey = redisKeyProvider.auth(email = userAuth.email)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(emailsKey, idsKey, authKey), // keys
            userAuth.email, // argv[1]
            userAuth.userId.toString(), // argv[2]
            objectMapper.writeValueAsString(userAuth) // argv[3]
        )
    }

    fun getUserCache(userId: UUID): CachedUser? {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        return redisTemplate.opsForValue().get(userCacheKey)
            ?.let { objectMapper.readValue(it, CachedUser::class.java) }
    }

    fun setUserCache(userId: UUID, cachedUser: CachedUser) {
        val userCacheKey = redisKeyProvider.user(userId = userId)

        redisTemplate.opsForValue().set(
            userCacheKey,
            objectMapper.writeValueAsString(cachedUser),
            3,
            TimeUnit.DAYS
        )
    }

    fun getRelationInfo(userId: UUID): RelationInfo {
        // settings
        val script = redisScriptProvider.getRelationInfo()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = userId)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(followingKey, followerKey), // keys
        )

        return RelationInfo(
            numOfFollowings = (result[0] as Long?) ?: 0L,
            numOfFollowers = (result[1] as Long?) ?: 0L
        )
    }
}
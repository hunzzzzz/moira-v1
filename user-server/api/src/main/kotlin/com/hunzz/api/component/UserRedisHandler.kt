package com.hunzz.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.dto.response.RelationInfo
import com.hunzz.common.exception.ErrorCode.DUPLICATED_EMAIL
import com.hunzz.common.exception.custom.InvalidSignupException
import com.hunzz.common.kafka.dto.KafkaSocialSignupRequest
import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.model.property.UserRole
import com.hunzz.common.model.property.UserType
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class UserRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userRedisScriptProvider: UserRedisScriptProvider
) {
    fun checkEmailDuplication(email: String) {
        val emailsKey = redisKeyProvider.emails()
        val isMember = redisTemplate.opsForSet().isMember(emailsKey, email) ?: true

        if (isMember) throw InvalidSignupException(DUPLICATED_EMAIL)
    }

    fun setSignupCode(email: String): String {
        val signupCodeKey = redisKeyProvider.signupCode(email = email)
        val signupCode = (10000..999999).random().toString()

        redisTemplate.opsForValue().set(
            signupCodeKey, signupCode, 3, TimeUnit.MINUTES
        )

        return signupCode
    }

    fun getSignupCode(email: String): String? {
        val signupCodeKey = redisKeyProvider.signupCode(email = email)

        return redisTemplate.opsForValue().get(signupCodeKey)
    }

    fun addEmailIntoRedisSet(email: String) {
        val emailsKey = redisKeyProvider.emails()

        redisTemplate.opsForSet().add(emailsKey, email)
    }

    fun socialUserSignup(request: KafkaSocialSignupRequest, type: UserType) {
        // 세팅
        val userAuth = UserAuth(
            userId = request.userId,
            type = type,
            role = UserRole.USER,
            email = request.email,
            password = null
        )
        val script = userRedisScriptProvider.signup()
        val emailsKey = redisKeyProvider.emails()
        val authKey = redisKeyProvider.auth(email = userAuth.email)

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, authKey),
            userAuth.email,
            objectMapper.writeValueAsString(userAuth)
        )
    }

    fun getRelationInfo(userId: UUID): RelationInfo {
        // 세팅
        val script = userRedisScriptProvider.getRelationInfo()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = userId)

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(followingKey, followerKey), // keys
        )

        // 데이터 변환
        return RelationInfo(
            numOfFollowings = (result[0] as Long?) ?: 0L,
            numOfFollowers = (result[1] as Long?) ?: 0L
        )
    }
}
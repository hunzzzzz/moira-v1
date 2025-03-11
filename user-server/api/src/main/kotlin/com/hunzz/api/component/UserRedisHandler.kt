package com.hunzz.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.api.dto.response.RelationInfo
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.exception.ErrorCode.*
import com.hunzz.common.exception.custom.InvalidSignupException
import com.hunzz.common.kafka.dto.KafkaSocialSignupRequest
import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.model.property.UserRole
import com.hunzz.common.model.property.UserType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRedisHandler(
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userRedisScriptProvider: UserRedisScriptProvider
) {
    fun validateSignupRequest(request: SignUpRequest) {
        // 1차 검증
        if (request.password != request.password2)
            throw InvalidSignupException(DIFFERENT_TWO_PASSWORDS)

        // 세팅
        val script = userRedisScriptProvider.validateSignupRequest()
        val emailsKey = redisKeyProvider.emails()
        val adminCodeKey = redisKeyProvider.adminCode()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, adminCodeKey),
            request.email,
            request.adminCode ?: ""
        )

        // 2차 검증
        when (result) {
            DUPLICATED_EMAIL.name -> throw InvalidSignupException(DUPLICATED_EMAIL)
            INVALID_ADMIN_CODE.name -> throw InvalidSignupException(INVALID_ADMIN_CODE)
        }
    }

    fun addUserData(userId: UUID, request: SignUpRequest) {
        // 세팅
        val userAuth = UserAuth(
            userId = userId,
            type = UserType.NORMAL,
            role = if (request.adminCode != null) UserRole.ADMIN else UserRole.USER,
            email = request.email!!,
            password = passwordEncoder.encodePassword(rawPassword = request.password!!)
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
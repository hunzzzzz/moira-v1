package com.hunzz.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.common.component.RedisKeyProvider
import com.hunzz.common.exception.ErrorCode.*
import com.hunzz.common.exception.custom.InvalidSignupException
import com.hunzz.common.kafka.dto.SocialSignupKafkaRequest
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

    fun signup(userId: UUID, request: SignUpRequest) {
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

    fun socialUserSignup(request: SocialSignupKafkaRequest, type: UserType) {
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
}
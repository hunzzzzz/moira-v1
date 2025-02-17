package com.hunzz.userserver.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.DUPLICATED_EMAIL
import com.hunzz.common.global.exception.ErrorCode.INVALID_ADMIN_CODE
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class UserRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun checkSignupRequest(inputEmail: String, inputAdminCode: String?) {
        // script
        val script = """
            local emails_key = KEYS[1]
            local admin_code_key = KEYS[2]
            local email = ARGV[1]
            local input_admin_code = ARGV[2]
            
            -- 이메일 중복 검증
            if redis.call('SISMEMBER', emails_key, email) == 1 then
                return 'DUPLICATED_EMAIL'
            end
            
            -- 어드민 코드 검증
            if input_admin_code ~= '' then
                local stored_admin_code = redis.call('GET', 'admin_signup_code')
                if stored_admin_code ~= input_admin_code then
                    return 'INVALID_ADMIN_CODE'
                end
            end
            
            return 'VALID'
        """.trimIndent()

        // redis keys
        val emailsKey = redisKeyProvider.emails()
        val adminCodeKey = redisKeyProvider.adminCode()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, adminCodeKey),
            inputEmail,
            inputAdminCode ?: ""
        )

        // check result
        when (result) {
            DUPLICATED_EMAIL.name -> throw InvalidUserInfoException(DUPLICATED_EMAIL)
            INVALID_ADMIN_CODE.name -> throw InvalidAdminRequestException(INVALID_ADMIN_CODE)
        }
    }

    fun signup(userAuth: UserAuth) {
        // script
        val script = """
            local emails_key = KEYS[1]
            local ids_key = KEYS[2]
            local authKey = KEYS[3]
            local email = ARGV[1]
            local id = ARGV[2]
            local userAuth = ARGV[3]
    
            -- 이메일 저장
            redis.call('SADD', emails_key, email)
            -- id 저장
            redis.call('SADD', ids_key, id)
            -- 유저 인증 정보 저장
            redis.call('SET', authKey, userAuth)
    
            return nil
        """.trimIndent()

        // redis keys
        val emailsKey = redisKeyProvider.emails()
        val idsKey = redisKeyProvider.ids()
        val authKey = redisKeyProvider.auth(email = userAuth.email)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, idsKey, authKey),
            userAuth.email,
            userAuth.userId.toString(),
            objectMapper.writeValueAsString(userAuth)
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
}
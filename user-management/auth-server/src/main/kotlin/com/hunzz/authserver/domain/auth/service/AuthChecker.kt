package com.hunzz.authserver.domain.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.BANNED_USER_CANNOT_LOGIN
import com.hunzz.common.global.exception.ErrorCode.INVALID_LOGIN_INFO
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.common.global.utility.RedisKeyProvider
import com.hunzz.common.global.utility.UserAuthProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class AuthChecker(
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userAuthProvider: UserAuthProvider
) {
    private val loginValidationScript = """
        local emails_key = KEYS[1]
        local banned_users_key = KEYS[2]
        local auth_key = KEYS[3]
        local email = ARGV[1]
        
        -- 이메일 검증
        if redis.call('SISMEMBER', emails_key, email) ~= 1 then
            return 'INVALID_LOGIN_INFO'
        end
        
        -- 차단 여부 확인
        if redis.call('ZSCORE', banned_users_key, email) then
            return 'BANNED_USER_CANNOT_LOGIN'
        end
        
        -- 유저 인증 정보 가져오기
        local user_auth = redis.call('GET', auth_key)
        if not user_auth then
            return 'NEEDS_REFRESH_AUTH_CACHE'
        end
        
        return user_auth
    """.trimIndent()

    fun checkLoginRequest(inputEmail: String, inputPassword: String): UserAuth {
        // redis keys
        val emailsKey = redisKeyProvider.emails()
        val bannedUsersKey = redisKeyProvider.bannedUsers()
        val authKey = redisKeyProvider.auth(email = inputEmail)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(loginValidationScript, String::class.java),
            listOf(emailsKey, bannedUsersKey, authKey),
            inputEmail,
        )

        // check result
        when (result) {
            INVALID_LOGIN_INFO.name -> {
                throw InvalidUserInfoException(INVALID_LOGIN_INFO)
            }

            BANNED_USER_CANNOT_LOGIN.name -> {
                throw InvalidAdminRequestException(BANNED_USER_CANNOT_LOGIN)
            }

            "NEEDS_REFRESH_AUTH_CACHE" -> {
                return userAuthProvider.getUserAuthWithLocalCache(email = inputEmail)
            }

            else -> {
                val userAuth = objectMapper.readValue(result, UserAuth::class.java)

                if (!passwordEncoder.matches(rawPassword = inputPassword, encodedPassword = userAuth.password))
                    throw InvalidUserInfoException(INVALID_LOGIN_INFO)
                else return userAuth!!
            }
        }
    }
}
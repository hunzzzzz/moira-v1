package com.hunzz.authserver.domain.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.*
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.common.global.utility.RedisKeyProvider
import com.hunzz.common.global.utility.UserAuthProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class AuthRedisHandler(
    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Long,

    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userAuthProvider: UserAuthProvider
) {
    fun checkLoginRequest(inputEmail: String, inputPassword: String): UserAuth {
        // script
        val script = """
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

        // redis keys
        val emailsKey = redisKeyProvider.emails()
        val bannedUsersKey = redisKeyProvider.bannedUsers()
        val authKey = redisKeyProvider.auth(email = inputEmail)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, bannedUsersKey, authKey),
            inputEmail
        )

        // check result
        return when (result) {
            INVALID_LOGIN_INFO.name -> throw InvalidUserInfoException(INVALID_LOGIN_INFO)
            BANNED_USER_CANNOT_LOGIN.name -> throw InvalidAdminRequestException(BANNED_USER_CANNOT_LOGIN)
            "NEEDS_REFRESH_AUTH_CACHE" -> userAuthProvider.getUserAuthWithLocalCache(email = inputEmail)

            else -> {
                val userAuth = objectMapper.readValue(result, UserAuth::class.java)

                if (!passwordEncoder.matches(rawPassword = inputPassword, encodedPassword = userAuth.password))
                    throw InvalidUserInfoException(INVALID_LOGIN_INFO)
                else userAuth!!
            }
        }
    }

    fun logout(atk: String, email: String) {
        // script
        val script = """
            local blocked_atk_key = KEYS[1]
            local rtk_key = KEYS[2]
            local atk = ARGV[1]
            local atk_exp_time = ARGV[2]
            
            -- 로그아웃 유저의 ATK 차단
            redis.call('SET', blocked_atk_key, atk, 'PX', atk_exp_time)
            
            -- RTK 삭제
            redis.call('DEL', rtk_key)
            
            return nil
        """.trimIndent()

        // redis keys
        val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
        val rtkKey = redisKeyProvider.rtk(email = email)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(blockedAtkKey, rtkKey),
            atk,
            expirationTimeOfAtk.toString()
        )
    }

    fun checkRtk(email: String, rtkFromAuthHeader: String): UserAuth {
        // script
        val script = """
            local rtk_key = KEYS[1]
            local auth_key = KEYS[2]
            local rtk_user = ARGV[1]
            
            -- RTK 검증
            local rtk_redis = redis.call('GET', rtk_key)
            if rtk_user ~= rtk_redis then
                return 'INVALID_TOKEN'
            end
            
            -- 유저 인증 정보 가져오기
            local user_auth = redis.call('GET', auth_key)
            if not user_auth then
                return 'NEEDS_REFRESH_AUTH_CACHE'
            end
            
            return user_auth
        """.trimIndent()

        // redis keys
        val rtkKey = redisKeyProvider.rtk(email = email)
        val authKey = redisKeyProvider.auth(email = email)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(rtkKey, authKey),
            rtkFromAuthHeader
        )

        // check result
        return when (result) {
            INVALID_TOKEN.name -> throw InvalidUserInfoException(INVALID_TOKEN)
            "NEEDS_REFRESH_AUTH_CACHE" -> userAuthProvider.getUserAuthWithLocalCache(email = email)

            else -> objectMapper.readValue(result, UserAuth::class.java)
        }
    }
}
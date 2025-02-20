package com.hunzz.authserver.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.exception.ErrorCode.*
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.AuthCacheManager
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class AuthRedisHandler(
    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Long,

    private val authCacheManager: AuthCacheManager,
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        const val REFRESH = "REFRESH"
    }

    fun validateLoginRequest(inputEmail: String, inputPassword: String): UserAuth {
        // script
        val script = redisScriptProvider.validateLoginRequest()
        val emailsKey = redisKeyProvider.emails()
        val bannedUsersKey = redisKeyProvider.bannedUsers()
        val authKey = redisKeyProvider.auth(email = inputEmail)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(emailsKey, bannedUsersKey, authKey), // keys
            inputEmail // argv[1]
        )

        // check result
        return when (result) {
            INVALID_LOGIN_INFO.name -> throw InvalidUserInfoException(INVALID_LOGIN_INFO)
            BANNED_USER_CANNOT_LOGIN.name -> throw InvalidAdminRequestException(BANNED_USER_CANNOT_LOGIN)
            REFRESH -> authCacheManager.getUserAuthWithLocalCache(email = inputEmail)
            else -> objectMapper.readValue(result, UserAuth::class.java)!!
        }
    }

    fun logout(atk: String, email: String) {
        // settings
        val script = redisScriptProvider.logout()
        val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
        val rtkKey = redisKeyProvider.rtk(email = email)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(blockedAtkKey, rtkKey), // keys
            atk, // argv[1]
            expirationTimeOfAtk.toString() // argv[2]
        )
    }

    fun checkRtk(email: String, rtkFromAuthHeader: String): UserAuth {
        // settings
        val script = redisScriptProvider.checkRtk()
        val rtkKey = redisKeyProvider.rtk(email = email)
        val authKey = redisKeyProvider.auth(email = email)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(rtkKey, authKey), // keys
            rtkFromAuthHeader // argv[1]
        )

        // check result
        return when (result) {
            INVALID_TOKEN.name -> throw InvalidUserInfoException(INVALID_TOKEN)
            REFRESH -> authCacheManager.getUserAuthWithLocalCache(email = email)
            else -> objectMapper.readValue(result, UserAuth::class.java)
        }
    }
}
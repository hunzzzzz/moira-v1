package com.hunzz.authserver.domain.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.exception.ErrorCode.*
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
import com.hunzz.authserver.utility.password.PasswordEncoder
import com.hunzz.authserver.utility.redis.RedisKeyProvider
import com.hunzz.authserver.utility.redis.RedisScriptProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class AuthRedisHandler(
    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Long,
    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTimeOfRtk: Long,

    private val authCacheManager: AuthCacheManager,
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        const val NO_CACHE = "NO_CACHE"
    }

    fun validateThenGetUserAuth(inputEmail: String, inputPassword: String): UserAuth {
        // 세팅
        val script = redisScriptProvider.validateThenGetUserAuth()
        val emailsKey = redisKeyProvider.emails()
        val authKey = redisKeyProvider.auth(email = inputEmail)

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(emailsKey, authKey),
            inputEmail
        )

        // 결과 확인
        val userAuth = when (result) {
            INVALID_LOGIN_INFO.name -> throw InvalidAuthException(INVALID_LOGIN_INFO)
            NO_CACHE -> authCacheManager.getUserAuthWithLocalCache(email = inputEmail)
            else -> objectMapper.readValue(result, UserAuth::class.java)
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword = inputPassword, encodedPassword = userAuth.password))
            throw InvalidAuthException(INVALID_LOGIN_INFO)

        return userAuth
    }

    fun setRtk(email: String, rtk: String) {
        // 세팅
        val rtkKey = redisKeyProvider.rtk(email = email)

        // Redis에 RTK 저장
        redisTemplate.opsForValue().set(rtkKey, rtk, expirationTimeOfRtk, TimeUnit.MILLISECONDS)
    }

    fun blockAtkThenDeleteRtk(atk: String, email: String) {
        // 세팅
        val script = redisScriptProvider.blockAtkThenDeleteRtk()
        val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
        val rtkKey = redisKeyProvider.rtk(email = email)

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(blockedAtkKey, rtkKey),
            atk,
            expirationTimeOfAtk.toString()
        )
    }

    fun checkRtkThenGetUserAuth(email: String, rtkFromAuthHeader: String): UserAuth {
        // 세팅
        val script = redisScriptProvider.checkRtkThenGetUserAuth()
        val rtkKey = redisKeyProvider.rtk(email = email)
        val authKey = redisKeyProvider.auth(email = email)

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(rtkKey, authKey),
            rtkFromAuthHeader
        )

        // 결과 확인
        return when (result) {
            EXPIRED_AUTH.name -> throw InvalidAuthException(EXPIRED_AUTH)
            INVALID_TOKEN.name -> throw InvalidAuthException(INVALID_TOKEN)
            NO_CACHE -> authCacheManager.getUserAuthWithLocalCache(email = email)
            else -> objectMapper.readValue(result, UserAuth::class.java)
        }
    }

    fun isNewcomer(email: String): Boolean {
        val emailsKey = redisKeyProvider.emails()
        val isNewcomer = !(redisTemplate.opsForSet().isMember(emailsKey, email) ?: true)

        return isNewcomer
    }
}
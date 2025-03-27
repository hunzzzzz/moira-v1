package com.hunzz.authserver.domain.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.exception.ErrorCode.*
import com.hunzz.authserver.utility.exception.custom.InvalidAuthException
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

    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun checkValidEmail(email: String) {
        val emailsKey = redisKeyProvider.emails()
        val isExistingEmail = redisTemplate.opsForSet().isMember(emailsKey, email) ?: false

        if (!isExistingEmail) throw InvalidAuthException(INVALID_LOGIN_INFO)
    }

    fun setRtk(email: String, rtk: String) {
        val rtkKey = redisKeyProvider.rtk(email = email)

        redisTemplate.opsForValue().set(
            rtkKey, rtk, expirationTimeOfRtk, TimeUnit.MILLISECONDS
        )
    }

    fun isNewcomer(email: String): Boolean {
        val emailsKey = redisKeyProvider.emails()
        val isNewcomer = !(redisTemplate.opsForSet().isMember(emailsKey, email) ?: true)

        return isNewcomer
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

    fun validateRtk(email: String, rtkFromAuthHeader: String) {
        // 세팅
        val script = redisScriptProvider.validateRtk()
        val rtkKey = redisKeyProvider.rtk(email = email)

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(rtkKey),
            rtkFromAuthHeader
        )

        // 결과 확인
        when (result) {
            EXPIRED_AUTH.name -> throw InvalidAuthException(EXPIRED_AUTH)
            INVALID_TOKEN.name -> throw InvalidAuthException(INVALID_TOKEN)
            else -> return
        }
    }
}
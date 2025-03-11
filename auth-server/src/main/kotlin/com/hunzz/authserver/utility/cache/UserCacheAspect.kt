package com.hunzz.authserver.utility.cache

import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.kafka.KafkaProducer
import com.hunzz.authserver.utility.kafka.dto.KafkaAddUserCacheRequest
import com.hunzz.authserver.utility.redis.RedisKeyProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class UserCacheAspect(
    private val jwtProvider: JwtProvider,
    private val kafkaProducer: KafkaProducer,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Around(value = "@annotation(com.hunzz.authserver.utility.cache.UserCache)")
    fun addUserCache(joinPoint: ProceedingJoinPoint): Any? {
        val returnValue = joinPoint.proceed()

        if (returnValue is TokenResponse) {
            // ATK에서 userId 추출
            val atk = jwtProvider.substringToken(token = returnValue.atk)!!
            val claims = jwtProvider.getUserInfoFromToken(token = atk)
            val userId = UUID.fromString(claims.subject)

            // Redis에 유저 캐시가 이미 존재하는지 확인
            // Kafka 메시지 전송 (auth-server -> user-cache)
            val userCacheKey = redisKeyProvider.user(userId = userId)
            val data = KafkaAddUserCacheRequest(userId = userId)

            if (redisTemplate.opsForValue().get(userCacheKey) == null)
                kafkaProducer.send(topic = "add-user-cache", data = data)
        }

        return returnValue
    }
}
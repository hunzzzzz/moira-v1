package com.hunzz.authserver.utility.cache

import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.utility.auth.JwtProvider
import com.hunzz.authserver.utility.kafka.KafkaProducer
import com.hunzz.authserver.utility.kafka.dto.KafkaAddUserCacheRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class UserCacheAspect(
    private val jwtProvider: JwtProvider,
    private val kafkaProducer: KafkaProducer
) {
    @Around(value = "@annotation(com.hunzz.authserver.utility.cache.UserCache)")
    fun addUserCache(joinPoint: ProceedingJoinPoint): Any? {
        val returnValue = joinPoint.proceed()

        if (returnValue is TokenResponse) {
            // ATK에서 userId 추출
            val atk = jwtProvider.substringToken(token = returnValue.atk) ?: return returnValue
            val claims = jwtProvider.getUserInfoFromToken(token = atk)
            val userId = UUID.fromString(claims.subject)

            // Kafka 메시지 전송 (auth-server -> user-cache)
            val data = KafkaAddUserCacheRequest(userId = userId)

            kafkaProducer.send(topic = "add-user-cache", data = data)
        }

        return returnValue
    }
}
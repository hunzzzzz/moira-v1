package com.hunzz.authserver.utility.idempotent

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
class IdempotencyAspect(
    private val httpServletRequest: HttpServletRequest,
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Around(value = "@annotation(com.hunzz.authserver.utility.idempotent.Idempotent)")
    fun around(joinPoint: ProceedingJoinPoint): Any? {
        // Authorization 헤더에서 Idempotency-Key값 추출
        val idempotencyKey = httpServletRequest.getHeader("Idempotency-Key")
            ?: return joinPoint.proceed()

        // Redis에서 Idempotency 객체 가져오기
        val idempotency = redisTemplate.opsForValue().get(idempotencyKey)
            ?.let { objectMapper.readValue(it, Idempotency::class.java) }

        // Idempotency 값이 존재하면 저장된 response 값을 그대로 리턴
        if (idempotency != null) {
            val responseClass = Class.forName(idempotency.className)
            val body = objectMapper.readValue(idempotency.response, responseClass)

            return ResponseEntity.ok(body)
        }
        // Idempotency 값이 존재하지 않으면, 로직 수행 후 response 값 저장
        else {
            val result = joinPoint.proceed() as ResponseEntity<*>
            val newIdempotency = Idempotency(
                className = (result.body!!)::class.java.canonicalName,
                response = objectMapper.writeValueAsString(result.body)
            )

            // Redis에 response 값 저장 (30초)
            redisTemplate.opsForValue().set(
                idempotencyKey,
                objectMapper.writeValueAsString(newIdempotency),
                30,
                TimeUnit.SECONDS
            )

            return result
        }
    }
}
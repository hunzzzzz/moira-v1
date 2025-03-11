package com.hunzz.api.cache

import com.hunzz.api.component.PostKafkaHandler
import com.hunzz.common.redis.RedisKeyProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class UserCacheAspect(
    private val postKafkaHandler: PostKafkaHandler,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Around(value = "@annotation(com.hunzz.api.cache.UserCache)")
    fun addUserCache(joinPoint: ProceedingJoinPoint): Any? {
        // 파라미터에서 userId 추출
        val signature = joinPoint.signature as MethodSignature
        val indexOfUserId = signature.parameterNames.indexOf("userId")
        val userId = joinPoint.args[indexOfUserId] as UUID

        // Redis에 유저 캐시가 이미 존재하는지 확인
        // 유저 캐시가 없다면, Kafka 메시지 전송 (post-api -> user-cache)
        val userCacheKey = redisKeyProvider.user(userId = userId)
        if (redisTemplate.opsForValue().get(userCacheKey) == null)
            postKafkaHandler.addUserCache(userId = userId)

        return joinPoint.proceed()
    }
}
package com.hunzz.relationserver.global.aop.cache

import com.hunzz.relationserver.global.utility.KafkaProducer
import com.hunzz.relationserver.global.utility.RedisKeyProvider
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class UserCacheAspect(
    private val kafkaProducer: KafkaProducer,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @AfterReturning(
        value = "@annotation(com.hunzz.relationserver.global.aop.cache.UserCache)"
    )
    fun addUserCache(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val userCache = signature.method.getAnnotation(UserCache::class.java)

        // get user-id
        val indexOfUserId = signature.parameterNames.indexOf(userCache.parameterName)
        val userId = joinPoint.args[indexOfUserId]?.let { it as UUID } ?: return

        // send kafka message
        val userKey = redisKeyProvider.user(userId = userId)

        if (redisTemplate.opsForValue().get(userKey) == null)
            kafkaProducer.send(topic = userCache.topic, data = userId)
    }
}
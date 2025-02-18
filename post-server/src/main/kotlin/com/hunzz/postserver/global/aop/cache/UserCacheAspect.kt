package com.hunzz.postserver.global.aop.cache

import com.hunzz.postserver.global.utility.KafkaProducer
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class UserCacheAspect(
    private val kafkaProducer: KafkaProducer
) {
    @AfterReturning(
        value = "@annotation(com.hunzz.postserver.global.aop.cache.UserCache)"
    )
    fun addUserCache(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val userCache = signature.method.getAnnotation(UserCache::class.java)

        // get user-id
        val indexOfUserId = signature.parameterNames.indexOf(userCache.parameterName)
        val userId = joinPoint.args[indexOfUserId]?.let { it as UUID } ?: return

        // send kafka message
        kafkaProducer.send(topic = userCache.topic, data = userId)
    }
}
package com.hunzz.postserver.global.aop.cache

import com.hunzz.postserver.global.utility.KafkaProducer
import com.hunzz.postserver.global.utility.RedisKeyProvider
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Aspect
@Component
class PostCacheAspect(
    private val kafkaProducer: KafkaProducer,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @AfterReturning(
        value = "@annotation(com.hunzz.postserver.global.aop.cache.PostCache)"
    )
    fun addUserCache(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val postCache = signature.method.getAnnotation(PostCache::class.java)

        // get post-id
        val indexOfPostId = signature.parameterNames.indexOf(postCache.parameterName)
        val postId = joinPoint.args[indexOfPostId]?.let { it as Long } ?: return

        // send kafka message
        val postCacheKey = redisKeyProvider.post(postId = postId)

        if (redisTemplate.opsForValue().get(postCacheKey) == null)
            kafkaProducer.send(topic = postCache.topic, data = postId)
    }
}
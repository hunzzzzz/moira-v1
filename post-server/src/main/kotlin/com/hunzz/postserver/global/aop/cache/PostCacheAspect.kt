package com.hunzz.postserver.global.aop.cache

import com.hunzz.postserver.global.utility.KafkaProducer
import com.hunzz.postserver.global.utility.RedisKeyProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
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
    private fun sendKafkaMessage(postCache: PostCache, postId: Long) {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        if (redisTemplate.opsForValue().get(postCacheKey) == null)
            kafkaProducer.send(topic = postCache.topic, data = postId)
    }

    @Around(
        value = "@annotation(com.hunzz.postserver.global.aop.cache.PostCache)"
    )
    fun addUserCache(joinPoint: ProceedingJoinPoint): Any? {
        val postId = joinPoint.proceed() as Long?

        val signature = joinPoint.signature as MethodSignature
        val postCache = signature.method.getAnnotation(PostCache::class.java)

        // if postId exists in return value
        if (postId != null)
            sendKafkaMessage(postCache = postCache, postId = postId)
        // if postId exists in parameter
        else {
            val indexOfPostId = signature.parameterNames.indexOf(postCache.parameterName)
            val newPostId = joinPoint.args[indexOfPostId]?.let { it as Long }

            sendKafkaMessage(postCache = postCache, postId = newPostId!!)
        }

        return postId
    }
}
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
class PostCacheAspect(
    private val postKafkaHandler: PostKafkaHandler,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private fun sendKafkaMessage(postId: UUID) {
        val postCacheKey = redisKeyProvider.post(postId = postId)

        if (redisTemplate.opsForValue().get(postCacheKey) == null)
            postKafkaHandler.addPostCache(postId = postId)
    }

    @Around(value = "@annotation(com.hunzz.api.cache.PostCache)")
    fun addPostCache(joinPoint: ProceedingJoinPoint): Any? {
        val returnValue = joinPoint.proceed()

        // postId가 파라미터에 있는 경우
        if (returnValue == null) {
            // 파라미터에서 postId 추출
            val signature = joinPoint.signature as MethodSignature
            val indexOfPostId = signature.parameterNames.indexOf("postId")
            val postId = joinPoint.args[indexOfPostId] as UUID

            sendKafkaMessage(postId = postId)
        }
        // postId가 리턴값에 있는 경우
        else if (returnValue is UUID)
            sendKafkaMessage(postId = returnValue)

        return returnValue
    }
}
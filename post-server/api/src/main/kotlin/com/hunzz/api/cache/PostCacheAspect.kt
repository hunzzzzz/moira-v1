package com.hunzz.api.cache

import com.hunzz.api.component.PostKafkaHandler
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class PostCacheAspect(
    private val postKafkaHandler: PostKafkaHandler
) {
    @Around(value = "@annotation(com.hunzz.api.cache.PostCache)")
    fun addPostCache(joinPoint: ProceedingJoinPoint): Any? {
        val returnValue = joinPoint.proceed()

        // postId가 파라미터에 있는 경우
        if (returnValue == null) {
            // 파라미터에서 postId 추출
            val signature = joinPoint.signature as MethodSignature
            val indexOfPostId = signature.parameterNames.indexOf("postId")
            val postId = joinPoint.args[indexOfPostId] as UUID

            postKafkaHandler.addPostCache(postId = postId)
        }
        // postId가 리턴값에 있는 경우
        else if (returnValue is UUID)
            postKafkaHandler.addPostCache(postId = returnValue)

        return returnValue
    }
}
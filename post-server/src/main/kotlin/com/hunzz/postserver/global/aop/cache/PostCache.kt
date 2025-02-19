package com.hunzz.postserver.global.aop.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostCache(
    val topic: String = "add-post-cache",
    val parameterName: String = "postId"
)
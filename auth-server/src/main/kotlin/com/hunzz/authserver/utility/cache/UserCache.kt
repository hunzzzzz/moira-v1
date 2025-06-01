package com.hunzz.authserver.utility.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserCache(
    val topic: String = "add-user-cache",
    val parameterName: String = "targetId"
)
package com.hunzz.authserver.utility.idempotent

@Target(AnnotationTarget.FUNCTION)
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Idempotent

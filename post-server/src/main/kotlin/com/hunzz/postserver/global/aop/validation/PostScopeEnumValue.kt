package com.hunzz.postserver.global.aop.validation

import com.hunzz.postserver.global.aop.validation.PostScopeEnumValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PostScopeEnumValidator::class])
annotation class PostScopeEnumValue(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
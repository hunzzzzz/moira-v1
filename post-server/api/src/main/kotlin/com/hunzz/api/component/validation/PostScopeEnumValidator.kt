package com.hunzz.api.component.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PostScopeEnumValidator : ConstraintValidator<PostScopeEnumValue, String> {
    private lateinit var postScopeEnumValue: PostScopeEnumValue

    override fun initialize(constraintAnnotation: PostScopeEnumValue) {
        this.postScopeEnumValue = constraintAnnotation
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false

        val enumValues = postScopeEnumValue.enumClass.java.enumConstants

        return enumValues?.any { value == it.toString() } ?: false
    }
}
package com.hunzz.moirav1.domain.post.dto.request

import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.global.aop.validation.PostScopeEnumValue
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_POST_CONTENT
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_POST_CONTENT
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_SCOPE
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostRequest(
    @field:NotBlank(message = UNWRITTEN_POST_CONTENT)
    @field:Size(min = 1, max = 500, message = INVALID_POST_CONTENT)
    var content: String?,

    @field:PostScopeEnumValue(enumClass = PostScope::class, message = UNWRITTEN_SCOPE)
    var scope: String?
)
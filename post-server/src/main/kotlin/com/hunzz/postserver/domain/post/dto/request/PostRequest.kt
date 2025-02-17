package com.hunzz.postserver.domain.post.dto.request

import com.hunzz.postserver.domain.post.model.PostScope
import com.hunzz.postserver.global.aop.validation.PostScopeEnumValue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostRequest(
    @field:NotBlank(message = "게시글 내용은 필수 입력 항목입니다.")
    @field:Size(min = 1, max = 500, message = "게시글은 1자 이상, 500자 이하로 작성해주세요.")
    var content: String?,

    @field:PostScopeEnumValue(enumClass = PostScope::class, message = "게시글 공개 범위는 필수 입력 항목입니다.")
    var scope: String?
)
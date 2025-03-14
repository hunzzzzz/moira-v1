package com.hunzz.api.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @field:Size(min = 1, max = 250, message = "댓글은 1자 이상, 250자 이하로 작성해주세요.")
    var content: String?
)
package com.hunzz.authserver.domain.dto.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    var email: String?,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    var password: String?
)
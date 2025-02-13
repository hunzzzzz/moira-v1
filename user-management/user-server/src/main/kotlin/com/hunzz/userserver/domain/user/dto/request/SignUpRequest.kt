package com.hunzz.userserver.domain.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    var email: String?,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @field:Pattern(
        message = "올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)",
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&.])[A-Za-z\\d@\$!%*?&]{8,16}\$"
    )
    var password: String?,

    @field:NotBlank(message = "한 번 더 비밀번호를 입력해주세요.")
    var password2: String?,

    @field:NotBlank(message = "이름은 필수 입력 항목입니다.")
    var name: String?,

    var adminCode: String? = null
)
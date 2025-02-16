package com.hunzz.moirav1.domain.user.dto.request

import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_PASSWORD
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_NAME
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_PASSWORD
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_PASSWORD2
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignUpRequest(
    @field:NotBlank(message = UNWRITTEN_EMAIL)
    @field:Email(message = INVALID_EMAIL)
    var email: String?,

    @field:NotBlank(message = UNWRITTEN_PASSWORD)
    @field:Pattern(
        message = INVALID_PASSWORD,
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&.])[A-Za-z\\d@\$!%*?&]{8,16}\$"
    )
    var password: String?,

    @field:NotBlank(message = UNWRITTEN_PASSWORD2)
    var password2: String?,

    @field:NotBlank(message = UNWRITTEN_NAME)
    var name: String?,

    var adminCode: String? = null
)
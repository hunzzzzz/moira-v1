package com.hunzz.moirav1.domain.user.dto.request

import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_PASSWORD
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = UNWRITTEN_EMAIL)
    var email: String?,

    @field:NotBlank(message = UNWRITTEN_PASSWORD)
    var password: String?
)
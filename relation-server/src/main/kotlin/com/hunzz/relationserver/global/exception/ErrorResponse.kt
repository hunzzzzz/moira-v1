package com.hunzz.relationserver.global.exception

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val statusCode: HttpStatus,
    val time: LocalDateTime = LocalDateTime.now()
)
package com.hunzz.userserver.controller

import com.hunzz.userserver.dto.request.SignUpRequest
import com.hunzz.userserver.service.UserHandler
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/signup")
class SignupController(
    private val userHandler: UserHandler
) {
    @PostMapping
    fun signup(@Valid @RequestBody request: SignUpRequest): ResponseEntity<UUID> {
        val body = userHandler.save(request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }
}
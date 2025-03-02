package com.hunzz.api.controller

import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.api.service.SignupService
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
    private val signupService: SignupService
) {
    @PostMapping
    fun signup(@Valid @RequestBody request: SignUpRequest): ResponseEntity<UUID> {
        val body = signupService.signup(request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }
}
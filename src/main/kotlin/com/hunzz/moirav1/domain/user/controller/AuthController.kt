package com.hunzz.moirav1.domain.user.controller

import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.domain.user.service.AuthHandler
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authHandler: AuthHandler
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val body = authHandler.login(request = request)

        return ResponseEntity.ok(body)
    }
}
package com.hunzz.api.controller

import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.api.service.SignupService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/signup")
class SignupController(
    private val signupService: SignupService
) {
    @PostMapping
    fun signup(
        @Valid @RequestBody request: SignUpRequest
    ): ResponseEntity<Unit> {
        val body = signupService.signup(request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @GetMapping("/code")
    fun sendSignupCode(
        @RequestParam email: String
    ): ResponseEntity<Unit> {
        val body = signupService.sendSignupCode(email = email)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/code/check")
    fun checkSignupCode(
        @RequestParam email: String,
        @RequestParam code: String
    ): ResponseEntity<Unit> {
        val body = signupService.checkSignupCode(email = email, code = code)

        return ResponseEntity.ok(body)
    }
}
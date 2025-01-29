package com.hunzz.moirav1.domain.user.controller

import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.domain.user.service.AuthHandler
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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

    @GetMapping("/logout")
    fun logout(@AuthPrincipal userAuth: UserAuth, httpServletRequest: HttpServletRequest): ResponseEntity<Unit> {
        val body = authHandler.logout(email = userAuth.email, httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }
}
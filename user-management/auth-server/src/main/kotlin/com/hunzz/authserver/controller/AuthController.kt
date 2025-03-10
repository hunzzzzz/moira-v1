package com.hunzz.authserver.controller

import com.hunzz.authserver.dto.request.LoginRequest
import com.hunzz.authserver.dto.response.TokenResponse
import com.hunzz.authserver.service.AuthHandler
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.aop.auth.AuthPrincipal
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
    fun logout(
        @AuthPrincipal userAuth: UserAuth,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<Unit> {
        val body = authHandler.logout(email = userAuth.email, httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/refresh")
    fun refresh(httpServletRequest: HttpServletRequest): ResponseEntity<TokenResponse> {
        val body = authHandler.refresh(httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }
}
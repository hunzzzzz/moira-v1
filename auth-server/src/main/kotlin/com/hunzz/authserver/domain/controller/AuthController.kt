package com.hunzz.authserver.domain.controller

import com.hunzz.authserver.domain.dto.request.LoginRequest
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.domain.service.LoginService
import com.hunzz.authserver.domain.service.LogoutService
import com.hunzz.authserver.domain.service.RefreshService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val loginService: LoginService,
    private val logoutService: LogoutService,
    private val refreshService: RefreshService
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val body = loginService.login(request = request)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/logout")
    fun logout(
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<Unit> {
        val body = logoutService.logout(httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/refresh")
    fun refresh(httpServletRequest: HttpServletRequest): ResponseEntity<TokenResponse> {
        val body = refreshService.refresh(httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }
}
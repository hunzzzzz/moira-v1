package com.hunzz.authserver.domain.controller

import com.hunzz.authserver.domain.dto.request.LoginRequest
import com.hunzz.authserver.domain.dto.response.TokenResponse
import com.hunzz.authserver.domain.service.*
import com.hunzz.authserver.utility.idempotent.Idempotent
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val kakaoLoginService: KakaoLoginService,
    private val loginService: LoginService,
    private val logoutService: LogoutService,
    private val naverLoginService: NaverLoginService,
    private val refreshService: RefreshService
) {
    @Idempotent
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<TokenResponse> {
        val body = loginService.login(request = request)

        return ResponseEntity.ok(body)
    }

    @Idempotent
    @GetMapping("/oauth/login/kakao")
    suspend fun kakaoLogin(@RequestParam code: String): ResponseEntity<TokenResponse> {
        val body = kakaoLoginService.kakaoLogin(code = code)

        return ResponseEntity.ok(body)
    }

    @Idempotent
    @GetMapping("/oauth/login/naver")
    suspend fun naverLogin(@RequestParam code: String): ResponseEntity<TokenResponse> {
        val body = naverLoginService.naverLogin(code = code)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/logout")
    fun logout(
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<Unit> {
        val body = logoutService.logout(httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }

    @Idempotent
    @GetMapping("/refresh")
    suspend fun refresh(httpServletRequest: HttpServletRequest): ResponseEntity<TokenResponse> {
        val body = refreshService.refresh(httpServletRequest = httpServletRequest)

        return ResponseEntity.ok(body)
    }
}
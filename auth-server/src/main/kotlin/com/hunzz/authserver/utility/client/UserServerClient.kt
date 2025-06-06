package com.hunzz.authserver.utility.client

import com.hunzz.authserver.utility.auth.UserAuth
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "user-server",
    url = "\${gateway.url}/user-cache/private"
)
interface UserServerClient {
    @GetMapping("/user/auth")
    fun getUserAuth(
        @RequestParam email: String
    ): UserAuth

    @GetMapping("/user/auth/validate")
    fun validateThenGetUserAuth(
        @RequestParam email: String,
        @RequestParam password: String
    ): UserAuth?
}
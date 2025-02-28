package com.hunzz.authserver.utility.client

import com.hunzz.authserver.utility.auth.UserAuth
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "user-server",
    url = "http://localhost:8080/user-server/private" // TODO : 추후 환경 변수로 분리
)
interface UserServerClient {
    @GetMapping("/user/auth")
    fun getUserAuth(
        @RequestParam email: String
    ): UserAuth
}
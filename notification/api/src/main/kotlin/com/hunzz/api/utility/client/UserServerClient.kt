package com.hunzz.api.utility.client

import com.hunzz.api.domain.dto.client.UserInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@FeignClient(
    name = "user-server",
    url = "http://localhost:8080/user-server/private" // TODO : 추후 환경 변수로 분리
)
interface UserServerClient {
    @GetMapping("/users/{userId}")
    fun getUserInfo(
        @PathVariable userId: UUID
    ): UserInfo
}
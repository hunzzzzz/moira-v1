package com.hunzz.api.client

import com.hunzz.api.client.dto.UserInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(
    name = "user-cache",
    url = "\${gateway.url}/user-cache/private"
)
interface UserServerClient {
    @GetMapping("/users")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): HashMap<UUID, UserInfo>
}
package com.hunzz.relationserver.utility.client

import com.hunzz.relationserver.domain.dto.response.FollowResponse
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
    ): HashMap<UUID, FollowResponse>

    @GetMapping("/users/all")
    fun getAllUserIds(): List<UUID>
}
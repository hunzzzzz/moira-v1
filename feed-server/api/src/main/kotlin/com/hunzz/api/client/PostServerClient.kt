package com.hunzz.api.client

import com.hunzz.api.client.dto.PostInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(
    name = "post-cache",
    url = "\${gateway.url}/post-cache/private"
)
interface PostServerClient {
    @GetMapping("/posts")
    fun getPosts(
        @RequestParam missingIds: List<UUID>
    ): HashMap<UUID, PostInfo>
}
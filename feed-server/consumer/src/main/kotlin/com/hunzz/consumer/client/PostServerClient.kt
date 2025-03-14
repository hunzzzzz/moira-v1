package com.hunzz.consumer.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(
    name = "post-server",
    url = "\${gateway.url}/post-cache/private"
)
interface PostServerClient {
    @GetMapping("/posts/latest")
    fun getLatestPostIds(
        @RequestParam authorId: UUID
    ): List<UUID>
}
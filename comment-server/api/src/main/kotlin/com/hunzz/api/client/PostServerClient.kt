package com.hunzz.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@FeignClient(
    name = "post-cache",
    url = "\${gateway.url}/post-cache/private"
)
interface PostServerClient {
    @GetMapping("/posts/{postId}/author")
    fun getPostAuthorId(
        @PathVariable postId: UUID
    ): UUID
}
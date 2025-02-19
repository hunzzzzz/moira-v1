package com.hunzz.feedserver.global.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(
    name = "post-server",
    url = "http://localhost:8080/post-server/private" // TODO : 추후 환경 변수로 분리
)
interface PostServerClient {
    @GetMapping("/posts")
    fun getPostIds(
        @RequestParam userId: UUID
    ): List<Long>
}
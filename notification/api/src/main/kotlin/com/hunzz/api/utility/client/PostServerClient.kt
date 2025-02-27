package com.hunzz.api.utility.client

import com.hunzz.api.domain.dto.client.CommentInfo
import com.hunzz.api.domain.dto.client.PostInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "post-server",
    url = "http://localhost:8080/post-server/private" // TODO : 추후 환경 변수로 분리
)
interface PostServerClient {
    @GetMapping("/posts/{postId}")
    fun getPostInfo(
        @PathVariable postId: Long
    ): PostInfo

    @GetMapping("/comments/{commentId}")
    fun getCommentInfo(
        @PathVariable commentId: Long
    ): CommentInfo
}
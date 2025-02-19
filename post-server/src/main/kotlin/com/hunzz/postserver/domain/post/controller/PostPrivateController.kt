package com.hunzz.postserver.domain.post.controller

import com.hunzz.postserver.domain.post.model.CachedPost
import com.hunzz.postserver.domain.post.service.PostHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/private")
class PostPrivateController(
    private val postHandler: PostHandler
) {
    @GetMapping("/posts")
    fun getPosts(
        @RequestParam postIds: List<Long>
    ): ResponseEntity<List<CachedPost>> {
        val body = postHandler.getAll(postIds)

        return ResponseEntity.ok(body)
    }
}
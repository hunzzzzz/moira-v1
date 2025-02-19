package com.hunzz.postserver.domain.post.controller

import com.hunzz.postserver.domain.post.service.PostHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/private")
class PostPrivateController(
    private val postHandler: PostHandler
) {
    @GetMapping("/posts")
    fun getPostIds(@RequestParam userId: UUID): ResponseEntity<List<Long>> {
        val body = postHandler.getAllIds(userId = userId)

        return ResponseEntity.ok(body)
    }
}
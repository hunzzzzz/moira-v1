package com.hunzz.postserver.domain.post.controller

import com.hunzz.postserver.domain.post.dto.request.PostRequest
import com.hunzz.postserver.domain.post.service.PostHandler
import com.hunzz.postserver.global.aop.auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/posts")
class PostController(
    private val postHandler: PostHandler
) {
    @PostMapping
    fun save(
        @AuthPrincipal userId: UUID,
        @Valid @RequestBody request: PostRequest
    ): ResponseEntity<Long> {
        val body = postHandler.save(userId = userId, request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @GetMapping("/{postId}/like")
    fun like(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long
    ): ResponseEntity<Unit> {
        val body = postHandler.like(userId = userId, postId = postId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{postId}/unlike")
    fun unlike(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long
    ): ResponseEntity<Unit> {
        val body = postHandler.unlike(userId = userId, postId = postId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{postId}")
    fun update(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostRequest
    ): ResponseEntity<Unit> {
        val body = postHandler.update(userId = userId, postId = postId, request = request)

        return ResponseEntity.ok(body)
    }

    @DeleteMapping("/{postId}")
    fun delete(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long,
    ): ResponseEntity<Unit> {
        val body = postHandler.delete(userId = userId, postId = postId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body)
    }
}
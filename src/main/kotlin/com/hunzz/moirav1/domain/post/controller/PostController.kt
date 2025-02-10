package com.hunzz.moirav1.domain.post.controller

import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.service.PostHandler
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/posts")
class PostController(
    private val postHandler: PostHandler
) {
    @PostMapping
    fun save(
        @AuthPrincipal userAuth: UserAuth,
        @Valid @RequestBody request: PostRequest
    ): ResponseEntity<Long> {
        val body = postHandler.save(userId = userAuth.userId, request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @PutMapping("/{postId}")
    fun update(
        @AuthPrincipal userAuth: UserAuth,
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostRequest
    ): ResponseEntity<Unit> {
        val body = postHandler.update(userId = userAuth.userId, postId = postId, request = request)

        return ResponseEntity.ok(body)
    }

    @DeleteMapping("/{postId}")
    fun delete(
        @AuthPrincipal userAuth: UserAuth,
        @PathVariable postId: Long,
    ): ResponseEntity<Unit> {
        val body = postHandler.delete(userId = userAuth.userId, postId = postId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body)
    }
}
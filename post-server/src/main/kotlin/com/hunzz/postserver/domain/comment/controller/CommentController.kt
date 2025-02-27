package com.hunzz.postserver.domain.comment.controller

import com.hunzz.postserver.domain.comment.dto.request.CommentRequest
import com.hunzz.postserver.domain.comment.dto.response.CommentSliceResponse
import com.hunzz.postserver.domain.comment.service.CommentHandler
import com.hunzz.postserver.global.aop.auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/posts/{postId}/comments")
class CommentController(
    private val commentHandler: CommentHandler
) {
    @PostMapping
    fun add(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long,
        @Valid @RequestBody request: CommentRequest
    ): ResponseEntity<Long> {
        val body = commentHandler.add(userId = userId, postId = postId, request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @GetMapping
    fun getAll(
        @PathVariable postId: Long,
        @RequestParam cursor: Long?
    ): ResponseEntity<CommentSliceResponse> {
        val body = commentHandler.getAll(postId = postId, cursor = cursor)

        return ResponseEntity.ok(body)
    }

    @DeleteMapping("/{commentId}")
    fun delete(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<Unit> {
        val body = commentHandler.delete(userId = userId, postId = postId, commentId = commentId)

        return ResponseEntity.ok(body)
    }
}
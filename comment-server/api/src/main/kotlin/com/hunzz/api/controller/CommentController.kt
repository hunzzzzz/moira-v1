package com.hunzz.api.controller

import com.hunzz.api.auth.AuthPrincipal
import com.hunzz.api.dto.request.CommentRequest
import com.hunzz.api.dto.response.CommentSliceResponse
import com.hunzz.api.service.AddCommentService
import com.hunzz.api.service.DeleteCommentService
import com.hunzz.api.service.GetCommentsService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/posts/{postId}/comments")
class CommentController(
    private val addCommentService: AddCommentService,
    private val deleteCommentService: DeleteCommentService,
    private val getCommentsService: GetCommentsService
) {
    @PostMapping
    suspend fun add(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @Valid @RequestBody request: CommentRequest
    ): ResponseEntity<UUID> {
        val body = addCommentService.addComment(userId = userId, postId = postId, request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @GetMapping
    suspend fun getAll(
        @PathVariable postId: UUID,
        @RequestParam cursor: UUID?
    ): ResponseEntity<CommentSliceResponse> {
        val body = getCommentsService.getComments(postId = postId, cursor = cursor)

        return ResponseEntity.ok(body)
    }

    @DeleteMapping("/{commentId}")
    fun delete(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID
    ): ResponseEntity<Unit> {
        val body = deleteCommentService.deleteComment(userId = userId, postId = postId, commentId = commentId)

        return ResponseEntity.ok(body)
    }
}
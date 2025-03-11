package com.hunzz.api.controller

import com.hunzz.api.auth.AuthPrincipal
import com.hunzz.api.dto.request.PostRequest
import com.hunzz.api.service.AddPostService
import com.hunzz.api.service.LikePostService
import com.hunzz.api.service.UpdatePostService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/posts")
class PostController(
    private val addPostService: AddPostService,
    private val likePostService: LikePostService,
    private val updatePostService: UpdatePostService
) {
    @PostMapping
    suspend fun addPost(
        @AuthPrincipal userId: UUID,
        @Valid @RequestPart request: PostRequest,
        @RequestPart(required = false) image: MultipartFile?
    ): ResponseEntity<UUID> {
        val body = addPostService.addPost(userId = userId, request = request, image = image)

        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @GetMapping("/{postId}/like")
    fun like(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID
    ): ResponseEntity<Unit> {
        val body = likePostService.like(userId = userId, postId = postId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{postId}/unlike")
    fun unlike(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID
    ): ResponseEntity<Unit> {
        val body = likePostService.unlike(userId = userId, postId = postId)

        return ResponseEntity.ok(body)
    }

    @PutMapping("/{postId}")
    suspend fun update(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID,
        @Valid @RequestBody request: PostRequest
    ): ResponseEntity<Unit> {
        val body = updatePostService.update(userId = userId, postId = postId, request = request)

        return ResponseEntity.ok(body)
    }

    @DeleteMapping("/{postId}")
    suspend fun delete(
        @AuthPrincipal userId: UUID,
        @PathVariable postId: UUID,
    ): ResponseEntity<Unit> {
        val body = updatePostService.delete(userId = userId, postId = postId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body)
    }
}
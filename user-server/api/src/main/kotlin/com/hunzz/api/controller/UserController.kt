package com.hunzz.api.controller

import com.hunzz.api.auth.AuthPrincipal
import com.hunzz.api.dto.response.UserResponse
import com.hunzz.api.service.ImageUploadService
import com.hunzz.api.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val imageUploadService: ImageUploadService,
    private val profileService: ProfileService
) {
    @GetMapping("/{targetId}")
    suspend fun get(
        @AuthPrincipal userId: UUID,
        @PathVariable targetId: UUID
    ): ResponseEntity<UserResponse> {
        val body = profileService.getProfile(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @PostMapping("/image")
    suspend fun uploadImage(
        @AuthPrincipal userId: UUID,
        @RequestPart image: MultipartFile
    ): ResponseEntity<Unit> {
        val body = imageUploadService.uploadImage(userId = userId, image = image)

        return ResponseEntity.ok(body)
    }
}
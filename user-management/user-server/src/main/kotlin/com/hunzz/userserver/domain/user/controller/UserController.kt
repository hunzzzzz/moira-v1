package com.hunzz.userserver.domain.user.controller

import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.global.aop.auth.AuthPrincipal
import com.hunzz.userserver.domain.user.dto.response.UserResponse
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userHandler: UserHandler
) {
    @GetMapping("/{targetId}")
    fun get(
        @AuthPrincipal userAuth: UserAuth,
        @PathVariable targetId: UUID
    ): ResponseEntity<UserResponse> {
        val body = userHandler.getProfile(userId = userAuth.userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @PostMapping("/image")
    fun uploadImage(
        @AuthPrincipal userAuth: UserAuth,
        @RequestPart image: MultipartFile
    ): ResponseEntity<Unit> {
        val body = userHandler.uploadImage(userId = userAuth.userId, image = image)

        return ResponseEntity.ok(body)
    }
}
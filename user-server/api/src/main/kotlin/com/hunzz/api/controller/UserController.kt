package com.hunzz.api.controller

import com.hunzz.api.auth.AuthPrincipal
import com.hunzz.api.dto.response.UserResponse
import com.hunzz.api.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val profileService: ProfileService
) {
    @GetMapping("/{targetId}")
    fun get(
        @AuthPrincipal userId: UUID,
        @PathVariable targetId: UUID
    ): ResponseEntity<UserResponse> {
        val body = profileService.getProfile(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }
}
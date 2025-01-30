package com.hunzz.moirav1.domain.user.controller

import com.hunzz.moirav1.domain.user.dto.response.UserResponse
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.domain.user.service.UserHandler
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userHandler: UserHandler
) {
    @GetMapping("/{targetId}")
    fun get(@AuthPrincipal userAuth: UserAuth, @PathVariable targetId: UUID): ResponseEntity<UserResponse> {
        val body = userHandler.get(userId = userAuth.userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }
}
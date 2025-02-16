package com.hunzz.userserver.global.client.controller

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/private")
class UserPrivateController(
    private val userHandler: UserHandler
) {
    @GetMapping("/users/{userId}/follow")
    fun getFollowInfo(
        @PathVariable userId: UUID
    ): ResponseEntity<CachedUser> {
        val body = userHandler.getWithLocalCache(userId = userId)

        return ResponseEntity.ok(body)
    }
}
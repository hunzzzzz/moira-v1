package com.hunzz.userserver.domain.user.controller

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/private")
class UserPrivateController(
    private val userHandler: UserHandler
) {
    @GetMapping("/users/{userId}")
    fun getUser(
        @PathVariable userId: UUID
    ): ResponseEntity<CachedUser> {
        val body = userHandler.getWithLocalCache(userId = userId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/users/all")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): ResponseEntity<HashMap<UUID, CachedUser>> {
        val body = userHandler.getAll(missingIds = missingIds)

        return ResponseEntity.ok(body)
    }
}
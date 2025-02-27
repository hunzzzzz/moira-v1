package com.hunzz.userserver.client

import com.hunzz.common.domain.user.model.CachedUser
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/private")
class UserPrivateController(
    private val userPrivateService: UserPrivateService
) {
    @GetMapping("/users/{userId}")
    fun getUser(
        @PathVariable userId: UUID
    ): ResponseEntity<CachedUser> {
        val body = userPrivateService.get(userId = userId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/users/all")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): ResponseEntity<HashMap<UUID, CachedUser>> {
        val body = userPrivateService.getAll(missingIds = missingIds)

        return ResponseEntity.ok(body)
    }
}
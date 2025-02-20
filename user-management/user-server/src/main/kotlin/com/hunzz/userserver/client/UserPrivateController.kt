package com.hunzz.userserver.client

import com.hunzz.common.domain.user.model.CachedUser
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/private")
class UserPrivateController(
    private val userPrivateService: UserPrivateService
) {
    @GetMapping("/users/all")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): ResponseEntity<HashMap<UUID, CachedUser>> {
        val body = userPrivateService.getAll(missingIds = missingIds)

        return ResponseEntity.ok(body)
    }
}
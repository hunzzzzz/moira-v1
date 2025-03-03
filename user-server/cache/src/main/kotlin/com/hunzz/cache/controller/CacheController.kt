package com.hunzz.cache.controller

import com.hunzz.cache.service.CacheService
import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.model.cache.UserInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/private")
class CacheController(
    private val cacheService: CacheService
) {
    @GetMapping("/user/auth")
    fun getUserAuth(
        @RequestParam email: String
    ): ResponseEntity<UserAuth> {
        val body = cacheService.getUserAuth(email = email)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/users")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): ResponseEntity<HashMap<UUID, UserInfo>> {
        val body = cacheService.getUsers(missingIds = missingIds)

        return ResponseEntity.ok(body)
    }
}
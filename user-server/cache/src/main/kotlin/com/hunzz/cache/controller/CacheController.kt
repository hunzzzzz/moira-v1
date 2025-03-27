package com.hunzz.cache.controller

import com.hunzz.cache.service.GetCachedUserService
import com.hunzz.cache.service.GetUserAuthService
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
    private val getUserAuthService: GetUserAuthService,
    private val getCachedUserService: GetCachedUserService
) {
    @GetMapping("/user/auth")
    fun getUserAuth(
        @RequestParam email: String
    ): ResponseEntity<UserAuth> {
        val body = getUserAuthService.getUserAuth(email = email)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/user/auth/validate")
    fun validateThenGetUserAuth(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<UserAuth> {
        val body = getUserAuthService.validateThenGetUserAuth(email = email, password = password)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/users")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): ResponseEntity<HashMap<UUID, UserInfo>> {
        val body = getCachedUserService.getUsers(missingIds = missingIds)

        return ResponseEntity.ok(body)
    }
}
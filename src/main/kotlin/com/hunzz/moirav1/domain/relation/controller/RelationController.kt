package com.hunzz.moirav1.domain.relation.controller

import com.hunzz.moirav1.domain.relation.service.RelationHandler
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/users")
class RelationController(
    private val relationHandler: RelationHandler
) {
    @GetMapping("/target/{targetId}/follow")
    fun follow(@AuthPrincipal userAuth: UserAuth, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = relationHandler.follow(userId = userAuth.userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/target/{targetId}/unfollow")
    fun unfollow(@AuthPrincipal userAuth: UserAuth, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = relationHandler.follow(userId = userAuth.userId, targetId = targetId, isUnfollow = true)

        return ResponseEntity.ok(body)
    }
}
package com.hunzz.relationserver.domain.relation.controller

import com.hunzz.relationserver.domain.relation.service.RelationHandler
import com.hunzz.relationserver.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/users/")
class RelationController(
    private val relationHandler: RelationHandler
) {
    @GetMapping("/target/{targetId}/follow")
    fun follow(@AuthPrincipal userId: UUID, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = relationHandler.follow(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/target/{targetId}/unfollow")
    fun unfollow(@AuthPrincipal userId: UUID, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = relationHandler.unfollow(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }
}

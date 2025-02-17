package com.hunzz.relationserver.domain.relation.controller

import com.hunzz.relationserver.domain.relation.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.relation.model.RelationType
import com.hunzz.relationserver.domain.relation.service.RelationHandler
import com.hunzz.relationserver.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @GetMapping("/{userId}/followings")
    fun getFollowings(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<FollowSliceResponse> {
        val body = relationHandler.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWING)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<FollowSliceResponse> {
        val body = relationHandler.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWER)

        return ResponseEntity.ok(body)
    }
}

package com.hunzz.moirav1.domain.relation.controller

import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import com.hunzz.moirav1.domain.relation.model.RelationType
import com.hunzz.moirav1.domain.relation.service.RelationHandler
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @GetMapping("/{userId}/followings")
    fun getFollowings(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<List<FollowResponse>> {
        val body = relationHandler.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWING)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<List<FollowResponse>> {
        val body = relationHandler.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWER)

        return ResponseEntity.ok(body)
    }
}
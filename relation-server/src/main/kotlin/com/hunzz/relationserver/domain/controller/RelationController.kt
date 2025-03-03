package com.hunzz.relationserver.domain.controller

import com.hunzz.relationserver.domain.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.model.RelationType
import com.hunzz.relationserver.domain.service.FollowService
import com.hunzz.relationserver.domain.service.GetRelationsService
import com.hunzz.relationserver.utility.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/users/")
class RelationController(
    private val followService: FollowService,
    private val getRelationsService: GetRelationsService
) {
    @GetMapping("/target/{targetId}/follow")
    fun follow(@AuthPrincipal userId: UUID, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = followService.follow(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/target/{targetId}/unfollow")
    fun unfollow(@AuthPrincipal userId: UUID, @PathVariable targetId: UUID): ResponseEntity<Unit> {
        val body = followService.unfollow(userId = userId, targetId = targetId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{userId}/followings")
    fun getFollowings(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<FollowSliceResponse> {
        val body = getRelationsService.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWING)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: UUID?
    ): ResponseEntity<FollowSliceResponse> {
        val body = getRelationsService.getRelations(userId = userId, cursor = cursor, type = RelationType.FOLLOWER)

        return ResponseEntity.ok(body)
    }
}

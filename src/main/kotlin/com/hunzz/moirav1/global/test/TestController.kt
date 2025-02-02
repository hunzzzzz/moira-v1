package com.hunzz.moirav1.global.test

import com.hunzz.moirav1.domain.relation.controller.RelationController
import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import org.springframework.data.domain.Slice
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/test")
class TestController(
    private val relationController: RelationController
) {
    @GetMapping("/users/{userId}/followings")
    fun getFollowings(
        @PathVariable userId: UUID,
        @RequestParam(required = false) cursor: LocalDateTime?
    ): ResponseEntity<Slice<FollowResponse>> {
        return relationController.getFollowings(userId = userId, cursor = cursor)
    }
}
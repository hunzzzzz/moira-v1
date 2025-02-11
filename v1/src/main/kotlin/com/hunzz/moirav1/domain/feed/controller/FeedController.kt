package com.hunzz.moirav1.domain.feed.controller

import com.hunzz.moirav1.domain.feed.dto.response.FeedResponse
import com.hunzz.moirav1.domain.feed.service.FeedHandler
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class FeedController(
    private val feedHandler: FeedHandler
) {
    @GetMapping
    fun getFeed(
        @AuthPrincipal userAuth: UserAuth,
        @RequestParam(required = false) cursor: Long?
    ): ResponseEntity<List<FeedResponse>> {
        val body = feedHandler.getFeed(userId = userAuth.userId, cursor = cursor)

        return ResponseEntity.ok(body)
    }
}
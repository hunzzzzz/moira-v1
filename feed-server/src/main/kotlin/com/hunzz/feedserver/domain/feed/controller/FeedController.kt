package com.hunzz.feedserver.domain.feed.controller

import com.hunzz.feedserver.domain.feed.dto.response.FeedSliceResponse
import com.hunzz.feedserver.domain.feed.service.FeedHandler
import com.hunzz.feedserver.global.aop.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/feed")
class FeedController(
    private val feedHandler: FeedHandler
) {
    @GetMapping
    fun getFeed(
        @AuthPrincipal userId: UUID,
        @RequestParam(required = false) cursor: Long?
    ): ResponseEntity<FeedSliceResponse> {
        val body = feedHandler.getFeed(userId = userId, cursor = cursor)

        return ResponseEntity.ok(body)
    }
}
package com.hunzz.api.controller

import com.hunzz.api.auth.AuthPrincipal
import com.hunzz.api.dto.response.FeedSliceResponse
import com.hunzz.api.service.FeedService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/feed")
class FeedController(
    private val feedService: FeedService
) {
    @GetMapping
    suspend fun getFeed(
        @AuthPrincipal userId: UUID,
        @RequestParam(required = false) cursor: Long?
    ): ResponseEntity<FeedSliceResponse> {
        val body = feedService.getFeed(userId = userId, cursor = cursor)

        return ResponseEntity.ok(body)
    }
}
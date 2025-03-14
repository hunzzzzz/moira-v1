package com.hunzz.cache.service

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/private")
class CacheController(
    private val cacheService: CacheService
) {
    @GetMapping("/posts/{postId}/author")
    fun getPostAuthorId(
        @PathVariable postId: UUID
    ): ResponseEntity<UUID> {
        val body = cacheService.getPostAuthorId(postId = postId)

        return ResponseEntity.ok(body)
    }

    @GetMapping("/posts/latest")
    fun getLatestPostIds(
        @RequestParam authorId: UUID
    ): ResponseEntity<List<UUID>> {
        val body = cacheService.getLatestPostIds(authorId = authorId)

        return ResponseEntity.ok(body)
    }
}
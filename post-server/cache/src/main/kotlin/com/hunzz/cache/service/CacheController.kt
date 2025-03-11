package com.hunzz.cache.service

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
}
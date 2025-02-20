package com.hunzz.imageserver.controller

import com.hunzz.imageserver.dto.ImageResponse
import com.hunzz.imageserver.service.ImageHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/images")
class ImageController(
    private val imageHandler: ImageHandler
) {
    @PostMapping
    fun upload(
        @RequestPart image: MultipartFile
    ): ResponseEntity<ImageResponse> {
        val body = imageHandler.save(image = image)

        return ResponseEntity.ok(body)
    }
}
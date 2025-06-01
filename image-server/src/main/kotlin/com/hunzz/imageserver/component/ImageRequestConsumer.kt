package com.hunzz.imageserver.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.imageserver.dto.KafkaImageRequest
import com.hunzz.imageserver.repository.ImageMetaRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class ImageRequestConsumer(
    private val imageMetaRepository: ImageMetaRepository,
    private val imageUploader: ImageUploader,
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(topics = ["upload-image"], groupId = "upload-image")
    fun upload(message: String) {
        val data = objectMapper.readValue(message, KafkaImageRequest::class.java)
        val originalImage = ImageIO.read(ByteArrayInputStream(data.image))

        imageUploader.uploadToS3(fileName = data.originalFileName, image = originalImage, scale = 1.0)
        imageUploader.uploadToS3(
            fileName = data.thumbnailFileName,
            image = originalImage,
            scale = 0.25,
            isThumbnail = true
        )
    }
}